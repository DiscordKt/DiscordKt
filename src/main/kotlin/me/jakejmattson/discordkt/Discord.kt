@file:Suppress("unused")

package me.jakejmattson.discordkt

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.request.KtorRequestException
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.*
import me.jakejmattson.discordkt.dsl.BotConfiguration
import me.jakejmattson.discordkt.dsl.Precondition
import me.jakejmattson.discordkt.dsl.diService
import me.jakejmattson.discordkt.extensions.pluralize
import me.jakejmattson.discordkt.internal.listeners.registerCommandListener
import me.jakejmattson.discordkt.internal.listeners.registerInteractionListener
import me.jakejmattson.discordkt.internal.utils.*
import me.jakejmattson.discordkt.locale.Locale
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass

/**
 * A collection of library properties read from internal library.properties file.
 *
 * @param version The current DiscordKt version.
 * @param kotlin The version of Kotlin used by DiscordKt.
 * @param kord The version of Kord used by DiscordKt.
 */
public data class LibraryProperties(val version: String, val kotlin: String, val kord: String) {
    /**
     * Print the version as a string in the form "$library - $kord - $kotlin"
     */
    override fun toString(): String = "$version - $kord - $kotlin"
}

/**
 * A collection of custom bot properties read from a bot.properties file.
 *
 * @param raw The full [Properties] object for additional properties.
 * @param name The name of the bot, retrieved by "name".
 * @param description A description of this bot, retrieved by "description".
 * @param url The repo url of the bot, retrieved by "url".
 * @param version The version of the bot, retrieved by "version".
 */
public data class BotProperties(val raw: Properties,
                                val name: String?,
                                val description: String?,
                                val url: String?,
                                val version: String?) {
    /**
     * Get the provided property from the raw Properties value.
     */
    public operator fun get(key: String): String? = raw.getProperty(key)
}

/**
 * Container for code properties.
 *
 * @property library Properties for the core library.
 * @property bot Properties for the current bot.
 * @property startup The [Instant] this bot started.
 */
public data class CodeProperties(val library: LibraryProperties, val bot: BotProperties, val startup: Instant = Instant.now())

/**
 * @property kord A Kord instance used to access the Discord API.
 * @property configuration All configured values for this bot.
 * @property locale Locale (language and customizations).
 * @property commands All registered commands.
 * @property subcommands All registered subcommands.
 * @property properties Properties for core and bot codebase.
 */
public abstract class Discord {
    public abstract val kord: Kord
    public abstract val configuration: BotConfiguration
    public abstract val locale: Locale
    public abstract val commands: MutableList<Command>
    public abstract val subcommands: MutableList<SubCommandSet>
    internal abstract val preconditions: MutableList<Precondition>

    public val properties: CodeProperties = CodeProperties(
        with(Properties().apply { load(LibraryProperties::class.java.getResourceAsStream("/library.properties")) }) {
            LibraryProperties(getProperty("version"), getProperty("kotlin"), getProperty("kord"))
        },
        run {
            val fileName = "bot.properties"
            val res = BotProperties::class.java.getResourceAsStream("/$fileName")

            if (res == null)
                BotProperties(Properties(), null, null, null, null)
            else
                with(Properties().apply { load(res) }) {
                    BotProperties(this, getProperty("name"), getProperty("description"), getProperty("url"), getProperty("version"))
                }
        }
    )

    /** Fetch an object from the DI pool by its type */
    public inline fun <reified A : Any> getInjectionObjects(): A = diService[A::class]

    /** Fetch an object from the DI pool by its type */
    public inline fun <reified A : Any> getInjectionObjects(a: KClass<A>): A = diService[a]

    /** Fetch an object from the DI pool by its type */
    public inline fun <reified A : Any, reified B : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>): Args2<A, B> =
        Args2(diService[a], diService[b])

    /** Fetch an object from the DI pool by its type */
    public inline fun <reified A : Any, reified B : Any, reified C : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>): Args3<A, B, C> =
        Args3(diService[a], diService[b], diService[c])

    /** Fetch an object from the DI pool by its type */
    public inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>): Args4<A, B, C, D> =
        Args4(diService[a], diService[b], diService[c], diService[d])

    /** Fetch an object from the DI pool by its type */
    public inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>, e: KClass<E>): Args5<A, B, C, D, E> =
        Args5(diService[a], diService[b], diService[c], diService[d], diService[e])

    @KordPreview
    internal suspend fun initCore() {
        diService.inject(this)
        val services = registerServices()
        Reflection.registerFunctions(this)
        registerListeners(this)

        if (configuration.logStartup) {
            val bot = properties.bot
            val name = bot.name ?: "DiscordKt"
            val version = bot.version ?: properties.library.version
            val header = "------- $name $version -------"

            InternalLogger.log(header)
            InternalLogger.log(commands.filterIsInstance<SlashCommand>().size.pluralize("Slash Command"))
            InternalLogger.log(commands.filterIsInstance<TextCommand>().size.pluralize("Text Command"))
            InternalLogger.log(subcommands.flatMap { it.commands }.size.pluralize("Subcommand"))
            InternalLogger.log(services.size.pluralize("Service"))
            InternalLogger.log(preconditions.size.pluralize("Precondition"))
            InternalLogger.log("-".repeat(header.length))

            if (properties.bot.raw.isEmpty)
                InternalLogger.error("Missing resources/bot.properties")
        }

        validate()

        commands.findByName(locale.helpName) ?: produceHelpCommand(locale.helpCategory).register(this)

        val (mentionEmbedName, mentionEmbedFun) = configuration.mentionEmbed

        if (mentionEmbedName != null && mentionEmbedFun != null)
            commands(locale.helpCategory) {
                slash(mentionEmbedName, "Bot info for ${properties.bot.name}", configuration.defaultPermissions) {
                    execute {
                        respondPublic {
                            mentionEmbedFun.invoke(this, this@execute.context)
                        }
                    }
                }
            }.register(this)

        registerSlashCommands()

        if (configuration.documentCommands)
            createDocumentation(commands, subcommands)
    }

    private fun registerServices() = Reflection.detectClassesWith<Service>().apply { diService.buildAllRecursively(this) }

    @KordPreview
    private suspend fun registerListeners(discord: Discord) {
        registerInteractionListener(discord)
        registerCommandListener(discord)
    }

    @KordPreview
    private suspend fun registerSlashCommands() {
        fun BaseInputChatBuilder.mapArgs(command: SlashCommand) {
            command.execution.arguments.forEach { argument ->
                val name = argument.name.lowercase()
                val description = argument.description

                data class ArgumentData(val argument: Argument<*, *>, val isRequired: Boolean, val isAutocomplete: Boolean)

                val (arg, isRequired, isAuto) = if (argument is WrappedArgument<*, *, *, *>) {
                    ArgumentData(
                        argument.innerType,
                        !argument.containsType<OptionalArg<*, *, *>>(),
                        argument.containsType<AutocompleteArg<*, *>>()
                    )
                } else
                    ArgumentData(argument, isRequired = true, isAutocomplete = false)

                when (arg) {
                    is ChoiceArg<*> -> string(name, description) {
                        required = isRequired

                        arg.choices.forEach {
                            choice(it.toString(), it.toString())
                        }
                    }

                    is AttachmentArgument<*> -> attachment(name, description) { required = isRequired }
                    is UserArgument<*> -> user(name, description) { required = isRequired }
                    is RoleArgument<*> -> role(name, description) { required = isRequired }
                    is ChannelArgument<*> -> channel(name, description) { required = isRequired }
                    is BooleanArgument<*> -> boolean(name, description) { required = isRequired }
                    is IntegerArgument<*> -> int(name, description) { required = isRequired; autocomplete = isAuto }
                    is DoubleArgument<*> -> number(name, description) { required = isRequired; autocomplete = isAuto }
                    else -> string(name, description) { required = isRequired; autocomplete = isAuto }
                }
            }
        }

        fun MultiApplicationCommandBuilder.register(command: SlashCommand) {
            if (command is ContextCommand) {
                when (command.execution.arguments.first()) {
                    is MessageArg -> message(command.displayText) { defaultMemberPermissions = command.requiredPermissions }
                    is UserArg -> user(command.displayText) { defaultMemberPermissions = command.requiredPermissions }
                    else -> {}
                }
            }

            input(command.name.lowercase(), command.description.ifBlank { "<No Description>" }) {
                mapArgs(command)
                defaultMemberPermissions = command.requiredPermissions
            }
        }

        val globalSlashCommands = commands.filterIsInstance<GlobalSlashCommand>()
        val guildSlashCommands = commands.filterIsInstance<GuildSlashCommand>()

        if (globalSlashCommands.isNotEmpty())
            kord.createGlobalApplicationCommands {
                globalSlashCommands.forEach {
                    register(it)
                }
            }

        if (guildSlashCommands.isNotEmpty())
            kord.guilds.toList().forEach { guild ->
                try {
                    guild.createApplicationCommands {
                        guildSlashCommands.forEach {
                            register(it)
                        }

                        subcommands.forEach {
                            input(it.name.lowercase(), it.name) {
                                defaultMemberPermissions = it.requiredPermissionLevel

                                it.commands.forEach { command ->
                                    subCommand(command.name.lowercase(), command.description.ifBlank { "<No Description>" }) {
                                        mapArgs(command)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: KtorRequestException) {
                    InternalLogger.error("[SLASH] ${Emojis.x.unicode} ${guild.name} - ${e.message}")
                }
            }
    }
}