package me.jakejmattson.kutils.internal.utils

import me.jakejmattson.kutils.api.annotations.*
import me.jakejmattson.kutils.api.buildDiscordClient
import me.jakejmattson.kutils.api.dsl.command.CommandsContainer
import me.jakejmattson.kutils.api.dsl.configuration.KConfiguration
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

class KUtils(private val config: KConfiguration, token: String, private val globalPath: String, private val enableScriptEngine: Boolean) {
    val discord = buildDiscordClient(token, config)
    private val conversationService: ConversationService = ConversationService(discord)
    internal var applyConfiguration = { init() }

    fun registerInjectionObjects(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    private fun init() {
        InternalLogger.startup("--------------- KUtils Startup ---------------")
        InternalLogger.startup("GlobalPath: $globalPath")
        discord.addEventListener(EventRegister)

        registerInjectionObjects(discord, conversationService, PersistenceService())

        if (enableScriptEngine)
            registerInjectionObjects(ScriptEngineService(discord))

        registerData()
        registerServices()

        val container = registerCommands()
        val preconditions = registerPreconditions()
        registerListeners(container, preconditions)

        conversationService.registerConversations(globalPath)

        createDocumentation(container)
        Validator.validateCommandMeta(container)
        Validator.validateReaction(config)

        InternalLogger.startup("----------------------------------------------")
    }

    @Suppress("UNUSED")
    fun configure(setup: KConfiguration.() -> Unit = {}) {
        applyConfiguration = {
            init()
            config.setup()
        }
    }

    private fun registerCommands(): CommandsContainer {
        val localContainer = ReflectionUtils.detectCommands(globalPath)

        //Add KUtils help command if a command named "Help" is not already provided
        val helpService = HelpService(localContainer, config)
        localContainer["Help"] ?: localContainer + helpService.produceHelpCommandContainer()

        CommandRecommender.addAll(localContainer.commands)

        return localContainer
    }

    private fun registerListeners(container: CommandsContainer, preconditions: MutableList<PreconditionData>): CommandListener {
        val listeners = ReflectionUtils.detectListeners(globalPath)

        InternalLogger.startup(listeners.size.pluralize("Listener"))

        fun registerListener(listener: Any) = EventRegister.eventBus.register(listener)

        val commandListener = CommandListener(container, discord, preconditions)

        registerListener(commandListener)
        listeners.forEach { registerListener(it) }

        return commandListener
    }

    private fun registerPreconditions() = ReflectionUtils.detectPreconditions(globalPath).also {
        InternalLogger.startup(it.size.pluralize("Precondition"))
    }.toMutableList()

    private fun registerServices() = diService.invokeDestructiveList(ReflectionUtils.detectClassesWith<Service>(globalPath))

    private fun registerData() {
        val data = ReflectionUtils.detectClassesWith<Data>(globalPath)
        val missingData = diService.collectDataObjects(data)

        InternalLogger.startup(data.size.pluralize("Data"))

        if (missingData.isEmpty()) return

        val dataString = missingData.joinToString(", ", postfix = ".")

        InternalLogger.error("The below data files were generated and must be filled in before re-running.\n$dataString")

        exitProcess(0)
    }
}