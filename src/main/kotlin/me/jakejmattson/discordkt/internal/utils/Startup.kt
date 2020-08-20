package me.jakejmattson.discordkt.internal.utils

import com.gitlab.kordlib.core.Kord
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.api.dsl.configuration.*
import me.jakejmattson.discordkt.api.dsl.data.Data
import me.jakejmattson.discordkt.api.dsl.preconditions.Precondition
import me.jakejmattson.discordkt.api.extensions.stdlib.pluralize
import me.jakejmattson.discordkt.api.services.ConversationService
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.listeners.*
import me.jakejmattson.discordkt.internal.services.*
import java.awt.Color
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()

/**
 * Backing class for [bot][me.jakejmattson.discordkt.api.dsl.bot] function.
 */
class Bot(private val token: String, private val globalPath: String) {
    private data class StartupFunctions(var configure: BotConfiguration.(Discord) -> Unit = { BotConfiguration() },
                                        var injection: InjectionConfiguration.() -> Unit = { InjectionConfiguration() },
                                        var logging: LoggingConfiguration.() -> Unit = { LoggingConfiguration() })

    private val startupBundle = StartupFunctions()
    private val botConfiguration = BotConfiguration()

    private suspend fun initCore(discord: Discord, loggingConfiguration: LoggingConfiguration) {
        diService.inject(discord)

        InternalLogger.shouldLogStartup = loggingConfiguration.showStartupLog
        val header = "------- DiscordKt ${discord.properties.libraryVersion} -------"
        InternalLogger.startup(header)

        val data = registerData()
        val conversationService = ConversationService(discord).apply { diService.inject(this) }
        val services = registerServices()
        val container = registerCommands()
        val preconditions = buildPreconditions().sortedBy { it.priority }

        registerCommandListener(discord, preconditions)
        registerReactionListener(discord.kord, conversationService)

        InternalLogger.startup(data.size.pluralize("Data"))
        InternalLogger.startup(services.size.pluralize("Service"))
        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        conversationService.registerConversations(globalPath)

        if (loggingConfiguration.generateCommandDocs)
            createDocumentation(container)

        Validator.validateCommandMeta(container)

        InternalLogger.startup("-".repeat(header.length))
    }

    internal suspend fun buildBot() {
        val (configureFun, injectionFun, loggingFun) = startupBundle

        val loggingConfiguration = LoggingConfiguration()
        loggingFun.invoke(loggingConfiguration)

        val injection = InjectionConfiguration()
        injectionFun.invoke(injection)

        val discord = buildDiscordClient(Kord(token), botConfiguration)
        initCore(discord, loggingConfiguration)
        configureFun.invoke(botConfiguration, discord)

        discord.kord.login()
    }

    /**
     * Modify core configuration.
     *
     * @sample BotConfiguration
     */
    @ConfigurationDSL
    fun configure(config: BotConfiguration.(Discord) -> Unit) {
        startupBundle.configure = config
    }

    /**
     * Inject objects into the dependency injection pool.
     *
     * @sample InjectionConfiguration
     */
    @ConfigurationDSL
    fun injection(config: InjectionConfiguration.() -> Unit) {
        startupBundle.injection = config
    }

    /**
     * Modify logging configuration.
     *
     * @sample LoggingConfiguration
     */
    @ConfigurationDSL
    fun logging(config: LoggingConfiguration.() -> Unit) {
        startupBundle.logging = config
    }

    private fun registerCommands(): MutableList<Command> {
        val commands = ReflectionUtils.detectCommands(globalPath)

        //Add help command if a command named "Help" is not already provided
        commands["Help"] ?: commands + produceHelpCommand(Color.BLUE).first()

        return commands
    }

    private fun registerServices() = ReflectionUtils.detectClassesWith<Service>(globalPath).apply { diService.buildAllRecursively(this) }
    private fun buildPreconditions() = ReflectionUtils.detectSubtypesOf<Precondition>(globalPath).map { diService.invokeConstructor(it) }

    private fun registerData() = ReflectionUtils
        .detectSubtypesOf<Data>(globalPath)
        .map {
            val default = it.getConstructor().newInstance()

            val data = with(default) {
                if (file.exists()) {
                    readFromFile()
                } else {
                    writeToFile()

                    if (killIfGenerated) {
                        InternalLogger.error("Please fill in the following file before re-running: ${file.absolutePath}")
                        exitProcess(-1)
                    }

                    this
                }
            }

            diService.inject(data)
        }
}