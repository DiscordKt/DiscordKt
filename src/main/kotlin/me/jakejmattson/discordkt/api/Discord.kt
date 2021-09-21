@file:Suppress("unused")

package me.jakejmattson.discordkt.api

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.request.KtorRequestException
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.commands.*
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.pluralize
import me.jakejmattson.discordkt.api.locale.Locale
import me.jakejmattson.discordkt.internal.listeners.registerCommandListener
import me.jakejmattson.discordkt.internal.listeners.registerInteractionListener
import me.jakejmattson.discordkt.internal.utils.*
import kotlin.reflect.KClass

/**
 * @param library The current DiscordKt version.
 * @param kotlin The version of Kotlin used by DiscordKt.
 * @param kord The version of Kord used by DiscordKt.
 */
@Serializable
data class Versions(val library: String, val kotlin: String, val kord: String)

/**
 * @property kord A Kord instance used to access the Discord API.
 * @property configuration All configured values for this bot.
 * @property locale Locale (language and customizations).
 * @property permissions Permission values and helper functions.
 * @property commands All registered commands.
 * @property versions Properties for the core library.
 */
abstract class Discord {
    abstract val kord: Kord
    abstract val configuration: BotConfiguration
    abstract val locale: Locale
    abstract val permissions: PermissionBundle
    abstract val commands: MutableList<Command>
    internal abstract val preconditions: MutableList<Precondition>

    @ExperimentalSerializationApi
    val versions = Json.decodeFromString<Versions>(javaClass.getResource("/library-properties.json")!!.readText())

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any> getInjectionObjects() = diService[A::class]

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any> getInjectionObjects(a: KClass<A>) = diService[a]

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>) =
        Args2(diService[a], diService[b])

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any, reified C : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>) =
        Args3(diService[a], diService[b], diService[c])

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>) =
        Args4(diService[a], diService[b], diService[c], diService[d])

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>, e: KClass<E>) =
        Args5(diService[a], diService[b], diService[c], diService[d], diService[e])

    @KordPreview
    internal suspend fun initCore() {
        diService.inject(this)
        val dataSize = registerData()
        val services = registerServices()

        ReflectionUtils.registerFunctions(configuration.packageName, this)
        registerListeners(this)

        if (configuration.showStartupLog) {
            val header = "----- DiscordKt ${versions.library} -----"
            val commandSets = commands.groupBy { it.category }.keys.size

            InternalLogger.log(header)
            InternalLogger.log(commandSets.pluralize("CommandSet") + " -> " + commands.size.pluralize("Command"))
            InternalLogger.log(dataSize.pluralize("Data"))
            InternalLogger.log(services.size.pluralize("Service"))
            InternalLogger.log(preconditions.size.pluralize("Precondition"))
            InternalLogger.log("-".repeat(header.length))
        }

        validate()
        registerSlashCommands()

        commands[locale.helpName] ?: produceHelpCommand(locale.helpCategory).register(this)

        if (configuration.generateCommandDocs)
            createDocumentation(commands)
    }

    private fun registerServices() = ReflectionUtils.detectClassesWith<Service>(configuration.packageName).apply { diService.buildAllRecursively(this) }

    @KordPreview
    private suspend fun registerListeners(discord: Discord) {
        registerInteractionListener(discord)
        registerCommandListener(discord)
    }

    @KordPreview
    private suspend fun registerSlashCommands() {
        fun ChatInputCreateBuilder.mapArgs(command: SlashCommand) {
            command.execution.arguments.forEach {
                val (arg, isRequired) = if (it is OptionalArg<*>) it.type to false else it to true
                val name = arg.name.lowercase()
                val description = arg.description

                when (arg) {
                    is IntegerArg -> int(name, description) { required = isRequired }
                    is DoubleArg -> number(name, description) { required = isRequired }
                    is BooleanArg -> boolean(name, description) { required = isRequired }
                    is UserArg, MemberArg -> user(name, description) { required = isRequired }
                    is RoleArg -> role(name, description) { required = isRequired }
                    is ChannelArg<*> -> channel(name, description) { required = isRequired }
                    else -> string(name, description) { required = isRequired }
                }
            }
        }

        fun MultiApplicationCommandBuilder.register(command: SlashCommand) {
            command.executions
                .filter { it.arguments.size == 1 }
                .forEach {
                    val potentialArg = it.arguments.first()

                    when (if (potentialArg is OptionalArg) potentialArg.type else potentialArg) {
                        MessageArg -> message(command.appName) {}
                        UserArg, MemberArg -> user(command.appName) {}
                    }
                }

            input(command.name.lowercase(), command.description.ifBlank { "<No Description>" }) {
                mapArgs(command)
            }
        }

        val globalSlashCommands = commands.filterIsInstance<GlobalSlashCommand>()
        val guildSlashCommands = commands.filterIsInstance<GuildSlashCommand>()

        kord.createGlobalApplicationCommands {
            globalSlashCommands.forEach {
                register(it)
            }
        }

        if (guildSlashCommands.isEmpty())
            return

        kord.guilds.toList().forEach { guild ->
            try {
                guild.createApplicationCommands {
                    guildSlashCommands.forEach {
                        register(it)
                    }
                }
            } catch (e: KtorRequestException) {
                InternalLogger.error("[SLASH] ${Emojis.x.unicode} ${guild.name} - ${e.message}")
            }
        }
    }

    private fun registerData() = ReflectionUtils.detectSubtypesOf<Data>(configuration.packageName)
        .map {
            val default = it.getConstructor().newInstance()

            val data = with(default) {
                if (!file.exists()) {
                    writeToFile()

                    if (killIfGenerated)
                        InternalLogger.fatalError("Please fill in the following file before re-running: ${file.absolutePath}")

                    this
                } else
                    readFromFile()
            }

            diService.inject(data)
        }.size

    /**
     * Get a [Command] of a given type by name.
     */
    inline fun <reified T : Command> commandOfType(name: String) = commands.filterIsInstance<T>().firstOrNull { cmd -> cmd.names.any { it.equals(name, true) } }
}