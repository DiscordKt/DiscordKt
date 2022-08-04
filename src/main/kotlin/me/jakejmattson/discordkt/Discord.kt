@file:Suppress("unused")

package me.jakejmattson.discordkt

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.request.KtorRequestException
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
import kotlin.reflect.KClass

/**
 * @param library The current DiscordKt version.
 * @param kotlin The version of Kotlin used by DiscordKt.
 * @param kord The version of Kord used by DiscordKt.
 */
@Serializable
public data class Versions(val library: String, val kotlin: String, val kord: String) {
    /**
     * Print the version as a string in the form "$library - $kord - $kotlin"
     */
    override fun toString(): String = "$library - $kord - $kotlin"
}

/**
 * @property kord A Kord instance used to access the Discord API.
 * @property configuration All configured values for this bot.
 * @property locale Locale (language and customizations).
 * @property commands All registered commands.
 * @property subcommands All registered subcommands.
 * @property versions Properties for the core library.
 */
public abstract class Discord {
    public abstract val kord: Kord
    public abstract val configuration: BotConfiguration
    public abstract val locale: Locale
    public abstract val commands: MutableList<Command>
    public abstract val subcommands: MutableList<SubCommandSet>
    internal abstract val preconditions: MutableList<Precondition>

    public val versions: Versions = Json.decodeFromString(javaClass.getResource("/library-properties.json")!!.readText())

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
            val header = "----- DiscordKt ${versions.library} -----"
            val commandSets = commands.groupBy { it.category }.keys.size

            InternalLogger.log(header)
            InternalLogger.log(commandSets.pluralize("CommandSet") + " -> " + commands.size.pluralize("Command"))
            InternalLogger.log(services.size.pluralize("Service"))
            InternalLogger.log(preconditions.size.pluralize("Precondition"))
            InternalLogger.log("-".repeat(header.length))
        }

        validate()

        commands[locale.helpName] ?: produceHelpCommand(locale.helpCategory).register(this)

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