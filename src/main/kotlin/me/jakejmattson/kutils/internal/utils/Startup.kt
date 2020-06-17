package me.jakejmattson.kutils.internal.utils

import me.jakejmattson.kutils.api.*
import me.jakejmattson.kutils.api.annotations.*
import me.jakejmattson.kutils.api.dsl.command.CommandsContainer
import me.jakejmattson.kutils.api.dsl.configuration.*
import me.jakejmattson.kutils.api.dsl.preconditions.PreconditionData
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import me.jakejmattson.kutils.api.services.*
import me.jakejmattson.kutils.internal.command.CommandRecommender
import me.jakejmattson.kutils.internal.event.EventRegister
import me.jakejmattson.kutils.internal.listeners.CommandListener
import me.jakejmattson.kutils.internal.services.*
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()

class KUtils(private val token: String, private val globalPath: String) {
    data class StartupFunctions(var client: ClientConfiguration.() -> Unit = { ClientConfiguration(token) },
                                var configure: BotConfiguration.(Discord) -> Unit = { BotConfiguration() },
                                var injecton: InjectionConfiguration.() -> Unit = { InjectionConfiguration() },
                                var logging: LoggingConfiguration.() -> Unit = { LoggingConfiguration() })

    private val startupBundle = StartupFunctions()
    private val botConfiguration = BotConfiguration()

    private fun initCore(discord: Discord, loggingConfiguration: LoggingConfiguration, enableScriptEngine: Boolean) {
        InternalLogger.shouldLogStartup = loggingConfiguration.showStartupLog

        InternalLogger.startup("--------------- KUtils Startup ---------------")
        InternalLogger.startup("GlobalPath: $globalPath")
        discord.addEventListener(EventRegister)

        val conversationService = ConversationService(discord)

        diService.addElement(discord)
        diService.addElement(conversationService)
        diService.addElement(PersistenceService())

        if (enableScriptEngine)
            diService.addElement(ScriptEngineService(discord))

        val data = registerData()
        InternalLogger.startup(data.size.pluralize("Data"))

        val services = detectServices()
        InternalLogger.startup(services.size.pluralize("Service"))
        registerServices(services)

        val container = registerCommands()

        val preconditions = detectPreconditions()
        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        registerListeners(discord, container, preconditions)

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

        val client = ClientConfiguration(token)
        clientFun.invoke(client)

        val injection = InjectionConfiguration()
        injectionFun.invoke(injection)

        val discord = buildDiscordClient(client.jdaBuilder, botConfiguration)
        initCore(discord, loggingConfiguration, injection.enableScriptEngineService)
        configureFun.invoke(botConfiguration, discord)
    }

    @Suppress("UNUSED")
    fun client(config: ClientConfiguration.() -> Unit) {
        println("Storing client info: $config")
        startupBundle.client = config
    }

    @Suppress("UNUSED")
    fun configure(config: BotConfiguration.(Discord) -> Unit) {
        startupBundle.configure = config
    }

    @Suppress("UNUSED")
    fun injection(config: InjectionConfiguration.() -> Unit) {
        startupBundle.injecton = config
    }

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

    private fun registerListeners(discord: Discord, container: CommandsContainer, preconditions: MutableList<PreconditionData>): CommandListener {
        val listeners = ReflectionUtils.detectListeners(globalPath)

        InternalLogger.startup(listeners.size.pluralize("Listener"))

        fun registerListener(listener: Any) = EventRegister.eventBus.register(listener)

        val commandListener = CommandListener(container, discord, botConfiguration, preconditions)

        registerListener(commandListener)
        listeners.forEach { registerListener(it) }

        return commandListener
    }

    private fun detectPreconditions() = ReflectionUtils.detectPreconditions(globalPath).toMutableList()
    private fun detectServices() = ReflectionUtils.detectClassesWith<Service>(globalPath)
    private fun registerServices(services: Set<Class<*>>) = diService.invokeDestructiveList(services)

    private fun registerData(): Set<Class<*>> {
        val data = ReflectionUtils.detectClassesWith<Data>(globalPath)
        val missingData = diService.collectDataObjects(data)

        if (missingData.isEmpty()) return data

        val dataString = missingData.joinToString(", ", postfix = ".")

        InternalLogger.error("The below data files were generated and must be filled in before re-running.\n$dataString")

        exitProcess(0)
    }
}