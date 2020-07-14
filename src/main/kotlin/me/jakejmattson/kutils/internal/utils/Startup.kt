package me.jakejmattson.kutils.internal.utils

import me.jakejmattson.kutils.api.*
import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.dsl.command.CommandsContainer
import me.jakejmattson.kutils.api.dsl.configuration.*
import me.jakejmattson.kutils.api.dsl.data.Data
import me.jakejmattson.kutils.api.dsl.preconditions.Precondition
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import me.jakejmattson.kutils.api.services.ConversationService
import me.jakejmattson.kutils.internal.command.CommandRecommender
import me.jakejmattson.kutils.internal.event.EventRegister
import me.jakejmattson.kutils.internal.listeners.*
import me.jakejmattson.kutils.internal.services.*
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()

class KUtils(private val token: String, private val globalPath: String) {
    private val jdaDefault
        get() = JDABuilder.createDefault(token)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)

    data class StartupFunctions(var client: (String) -> JDABuilder,
                                var configure: BotConfiguration.(Discord) -> Unit = { BotConfiguration() },
                                var injecton: InjectionConfiguration.() -> Unit = { InjectionConfiguration() },
                                var logging: LoggingConfiguration.() -> Unit = { LoggingConfiguration() })

    private val startupBundle = StartupFunctions({ jdaDefault })
    private val botConfiguration = BotConfiguration()

    private fun initCore(discord: Discord, loggingConfiguration: LoggingConfiguration) {
        InternalLogger.shouldLogStartup = loggingConfiguration.showStartupLog

        InternalLogger.startup("--------------- KUtils Startup ---------------")
        InternalLogger.startup("GlobalPath: $globalPath")
        discord.addEventListener(EventRegister)

        val conversationService = ConversationService(discord)

        diService.inject(discord)
        diService.inject(conversationService)

        val data = registerData()
        InternalLogger.startup(data.size.pluralize("Data"))

        val services = detectServices()
        InternalLogger.startup(services.size.pluralize("Service"))
        registerServices(services)

        val container = registerCommands()

        val preconditions = detectPreconditions()
        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        registerListeners(discord, container, preconditions, conversationService)

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

        val discord = buildDiscordClient(jdaBuilder, botConfiguration)
        initCore(discord, loggingConfiguration)
        configureFun.invoke(botConfiguration, discord)
    }

    /**
     * Modify client configuration.
     */
    @Suppress("UNUSED")
    fun client(config: (String) -> JDABuilder) {
        startupBundle.client = config
    }

    /**
     * Modify core configuration.
     *
     * @sample BotConfiguration
     */
    @Suppress("UNUSED")
    fun configure(config: BotConfiguration.(Discord) -> Unit) {
        startupBundle.configure = config
    }

    /**
     * Inject objects into the dependency injection pool.
     *
     * @sample InjectionConfiguration
     */
    @Suppress("UNUSED")
    fun injection(config: InjectionConfiguration.() -> Unit) {
        startupBundle.injecton = config
    }

    /**
     * Modify logging configuration.
     *
     * @sample LoggingConfiguration
     */
    @Suppress("UNUSED")
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

    private fun registerListeners(discord: Discord, container: CommandsContainer, preconditions: List<Precondition>, conversationService: ConversationService): CommandListener {
        val listeners = ReflectionUtils.detectListeners(globalPath)

        InternalLogger.startup(listeners.size.pluralize("Listener"))

        fun registerListener(listener: Any) = EventRegister.eventBus.register(listener)

        val commandListener = CommandListener(container, discord, preconditions)
        val reactionListener = ReactionListener(conversationService)

        registerListener(commandListener)
        registerListener(reactionListener)
        listeners.forEach { registerListener(it) }

        return commandListener
    }

    private fun detectPreconditions() = ReflectionUtils.detectSubtypesOf<Precondition>(globalPath).map { diService.invokeConstructor(it) }
    private fun detectServices() = ReflectionUtils.detectClassesWith<Service>(globalPath)
    private fun registerServices(services: Set<Class<*>>) = diService.buildAllRecursively(services)

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