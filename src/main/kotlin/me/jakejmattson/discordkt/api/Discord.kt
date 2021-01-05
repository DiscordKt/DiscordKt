@file:Suppress("unused")

package me.jakejmattson.discordkt.api

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.pluralize
import me.jakejmattson.discordkt.internal.listeners.*
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
 * @property api A Kord instance used to access the Discord API.
 * @property configuration All of the current configuration details for this bot.
 * @property commands All registered commands.
 * @property versions Properties for the core library.
 */
abstract class Discord {
    abstract val api: Kord
    abstract val configuration: BotConfiguration
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
        val showStartupLog = configuration.showStartupLog
        val generateCommandDocs = configuration.generateCommandDocs
        val header = "----- DiscordKt ${versions.library} -----"

        if (showStartupLog)
            InternalLogger.log(header)

        val dataSize = registerData()
        val services = registerServices()

        ReflectionUtils.registerFunctions(configuration.packageName, this)
        registerSlashCommands()
        registerReactionListener(api)
        registerCommandListener(this)

        val commandSets = commands.groupBy { it.category }.keys.size

        if (showStartupLog) {
            InternalLogger.log(commandSets.pluralize("CommandSet") + " -> " + commands.size.pluralize("Command"))
            InternalLogger.log(dataSize.pluralize("Data"))
            InternalLogger.log(services.size.pluralize("Service"))
            InternalLogger.log(preconditions.size.pluralize("Precondition"))
        }

        registerHelpCommand(this)

        if (generateCommandDocs)
            createDocumentation(commands)

        Validator.validateCommandMeta(commands)

        if (showStartupLog)
            InternalLogger.log("-".repeat(header.length))
    }

    private fun registerServices() = ReflectionUtils.detectClassesWith<Service>(configuration.packageName).apply { diService.buildAllRecursively(this) }
    private fun registerHelpCommand(discord: Discord) = discord.commands["Help"]
        ?: produceHelpCommand().register(discord)

    @KordPreview
    private suspend fun registerSlashCommands() {
        commands.filterIsInstance<SlashCommand>().forEach {
            api.createGlobalApplicationCommand(it.names.first(), it.description) {
                it.executions.first().arguments.forEach {
                    when (it) {
                        is IntegerArg -> int(it.name, "")
                        is BooleanArg -> boolean(it.name, "")
                        is UserArg -> user(it.name, "")
                        is RoleArg -> role(it.name, "")
                        is ChannelArg<*> -> channel(it.name, "")
                        else -> string(it.name, "")
                    }
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