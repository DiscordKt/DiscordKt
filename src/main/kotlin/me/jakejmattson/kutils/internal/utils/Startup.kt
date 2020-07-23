package me.jakejmattson.kutils.internal.utils

import com.google.common.eventbus.EventBus
import me.jakejmattson.kutils.api.*
import me.jakejmattson.kutils.api.annotations.*
import me.jakejmattson.kutils.api.dsl.command.CommandsContainer
import me.jakejmattson.kutils.api.dsl.configuration.*
import me.jakejmattson.kutils.api.dsl.data.Data
import me.jakejmattson.kutils.api.dsl.preconditions.Precondition
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import me.jakejmattson.kutils.api.services.ConversationService
import me.jakejmattson.kutils.internal.command.CommandRecommender
import me.jakejmattson.kutils.internal.listeners.*
import me.jakejmattson.kutils.internal.services.*
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()

/**
 * Backing class for [bot][me.jakejmattson.kutils.api.dsl.bot] function.
 */
class Bot(private val token: String, private val globalPath: String) {
    private val jdaDefault = JDABuilder.createDefault(token)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .enableIntents(GatewayIntent.GUILD_MEMBERS)

    private data class StartupFunctions(var client: (String) -> JDABuilder,
                                        var configure: BotConfiguration.(Discord) -> Unit = { BotConfiguration() },
                                        var injecton: InjectionConfiguration.() -> Unit = { InjectionConfiguration() },
                                        var logging: LoggingConfiguration.() -> Unit = { LoggingConfiguration() })

    private val startupBundle = StartupFunctions({ jdaDefault })
    private val botConfiguration = BotConfiguration()
    private val eventBus = EventBus()

    private fun initCore(discord: Discord, loggingConfiguration: LoggingConfiguration) {
        diService.inject(discord)

        InternalLogger.shouldLogStartup = loggingConfiguration.showStartupLog
        InternalLogger.startup("--------------- KUtils Startup ---------------")
        InternalLogger.startup("GlobalPath: $globalPath")

        val data = registerData()
        val conversationService = ConversationService(discord).apply { diService.inject(this) }
        val services = registerServices()
        val container = registerCommands()
        val listeners = detectListeners()
        val preconditions = buildPreconditions().sortedBy { it.priority }
        val commandListener = CommandListener(container, discord, preconditions)
        val reactionListener = ReactionListener(conversationService)
        val allListeners = listeners + listOf(commandListener, reactionListener)

        InternalLogger.startup(data.size.pluralize("Data"))
        InternalLogger.startup(services.size.pluralize("Service"))
        InternalLogger.startup(listeners.size.pluralize("Listener"))
        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        allListeners.forEach { eventBus.register(it) }
        conversationService.registerConversations(globalPath)

        if (loggingConfiguration.generateCommandDocs)
            createDocumentation(container)

        Validator.validateCommandMeta(container)
        Validator.validateReaction(botConfiguration)

        InternalLogger.startup("----------------------------------------------")
    }

    internal fun buildBot() {
        val (clientFun,
            configureFun,
            injectionFun,
            loggingFun
        ) = startupBundle

        val loggingConfiguration = LoggingConfiguration()
        loggingFun.invoke(loggingConfiguration)

        val jdaBuilder = clientFun.invoke(token)

        val injection = InjectionConfiguration()
        injectionFun.invoke(injection)

        val discord = buildDiscordClient(jdaBuilder, botConfiguration, eventBus)
        initCore(discord, loggingConfiguration)
        configureFun.invoke(botConfiguration, discord)
    }

    /**
     * Modify client configuration.
     */
    @ConfigurationDSL
    fun client(config: (String) -> JDABuilder) {
        startupBundle.client = config
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
        startupBundle.injecton = config
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

    private fun registerCommands(): CommandsContainer {
        val localContainer = ReflectionUtils.detectCommands(globalPath)

        //Add KUtils help command if a command named "Help" is not already provided
        val helpService = HelpService(localContainer, botConfiguration)
        localContainer["Help"] ?: localContainer + helpService.produceHelpCommandContainer()

        CommandRecommender.addAll(localContainer.commands)

        return localContainer
    }

    private fun detectListeners() = ReflectionUtils.detectListeners(globalPath)
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
                    if (killIfGenerated) {
                        InternalLogger.error("Please fill in the following file before re-running: ${file.absolutePath}")
                        writeToFile()
                        exitProcess(-1)
                    }

                    this
                }
            }

            diService.inject(data)
        }
}