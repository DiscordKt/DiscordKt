@file:Suppress("unused")

package me.jakejmattson.discordkt.api

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommand
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.pluralize
import me.jakejmattson.discordkt.api.locale.Locale
import me.jakejmattson.discordkt.internal.listeners.registerCommandListener
import me.jakejmattson.discordkt.internal.listeners.registerInteractionListener
import me.jakejmattson.discordkt.internal.utils.*
import kotlin.reflect.KClass
import kotlin.system.exitProcess

/**
 * @param library The current DiscordKt version.
 * @param kotlin The version of Kotlin used by DiscordKt.
 * @param kord The version of Kord used by DiscordKt.
 */
@Serializable
data class Versions(val library: String, val kotlin: String, val kord: String)

/**
 * @property kord A Kord instance used to access the Discord API.
 * @property configuration All of the current configuration details for this bot.
 * @property locale Locale (language and customizations).
 * @property commands All registered commands.
 * @property versions Properties for the core library.
 */
abstract class Discord {
    abstract val kord: Kord
    abstract val configuration: BotConfiguration
    abstract val locale: Locale
    abstract val commands: MutableList<Command>
    internal abstract val preconditions: MutableList<Precondition>
    val versions = Json.decodeFromString<Versions>(javaClass.getResource("/library-properties.json").readText())

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

        Validator.validateCommands(commands)
        Validator.validateArgumentTypes(commands)
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
        fun ApplicationCommandCreateBuilder.unpack(command: Command) {
            command.executions.first().arguments.forEach { arg ->
                when (arg) {
                    is IntegerArg -> int(arg.name, arg.description)
                    is BooleanArg -> boolean(arg.name, arg.description)
                    is UserArg -> user(arg.name, arg.description)
                    is RoleArg -> role(arg.name, arg.description)
                    is ChannelArg<*> -> channel(arg.name, arg.description)
                    else -> string(arg.name, arg.description)
                }
            }
        }

        commands.filterIsInstance<GlobalSlashCommand>().forEach { slashCommand ->
            kord.createGlobalApplicationCommand(slashCommand.name, slashCommand.description.ifBlank { "<No Description>" }) {
                unpack(slashCommand)
            }
        }

        commands.filterIsInstance<GuildSlashCommand>().forEach { slashCommand ->
            kord.guilds.onEach { guild ->
                guild.createApplicationCommand(slashCommand.name, slashCommand.description.ifBlank { "<No Description>" }) {
                    unpack(slashCommand)
                }
            }
        }
    }

    private fun registerData() = ReflectionUtils.detectSubtypesOf<Data>(configuration.packageName)
        .map {
            val default = it.getConstructor().newInstance()

            val data = with(default) {
                if (!file.exists()) {
                    writeToFile()

                    if (killIfGenerated) {
                        InternalLogger.error("Please fill in the following file before re-running: ${file.absolutePath}")
                        exitProcess(-1)
                    }

                    this
                } else
                    readFromFile()
            }

            diService.inject(data)
        }.size
}