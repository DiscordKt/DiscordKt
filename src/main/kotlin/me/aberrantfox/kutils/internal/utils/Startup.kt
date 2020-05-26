package me.aberrantfox.kutils.internal.utils

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kutils.api.annotations.*
import me.aberrantfox.kutils.api.buildDiscordClient
import me.aberrantfox.kutils.api.dsl.command.*
import me.aberrantfox.kutils.api.dsl.configuration.KConfiguration
import me.aberrantfox.kutils.api.dsl.preconditions.*
import me.aberrantfox.kutils.api.extensions.stdlib.pluralize
import me.aberrantfox.kutils.api.services.*
import me.aberrantfox.kutils.internal.command.CommandRecommender
import me.aberrantfox.kutils.internal.event.EventRegister
import me.aberrantfox.kutils.internal.listeners.CommandListener
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()

class KUtils(private val config: KConfiguration, token: String, private val globalPath: String, enableScriptEngine: Boolean) {
    val discord = buildDiscordClient(token, config)
    private val conversationService: ConversationService = ConversationService(discord)

    init {
        InternalLogger.startup("--------------- KUtils Startup ---------------")
        InternalLogger.startup("GlobalPath: $globalPath")
        discord.addEventListener(EventRegister)

        registerInjectionObjects(discord, conversationService)

        if (enableScriptEngine)
            registerInjectionObjects(ScriptEngineService(discord))

        detectData()
        detectServices()

        val container = registerCommands()
        val preconditions = registerPreconditions().toMutableList()
        registerListeners(container, preconditions)

        conversationService.registerConversations(globalPath)

        createDocumentation(container)
        Validator.validateCommandMeta(container)
        Validator.validateReaction(config)
    }

    fun registerInjectionObjects(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun configure(setup: KConfiguration.() -> Unit = {}) {
        config.setup()
    }

    private fun registerCommands(): CommandsContainer {
        val localContainer = produceContainer(globalPath, diService)

        //Add KUtils help command if a command named "Help" is not already provided
        val helpService = HelpService(localContainer, config)
        localContainer["Help"] ?: localContainer + helpService.produceHelpCommandContainer()

        CommandRecommender.addAll(localContainer.commands)

        return localContainer
    }

    private fun registerListeners(container: CommandsContainer, preconditions: MutableList<PreconditionData>): CommandListener {
        val listeners = Reflections(globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Subscribe::class.java)
            .map { it.declaringClass }
            .distinct()
            .map { diService.invokeConstructor(it) }

        InternalLogger.startup(listeners.size.pluralize("Listener"))

        fun registerListener(listener: Any) = EventRegister.eventBus.register(listener)

        val commandListener = CommandListener(container, discord, preconditions)

        registerListener(commandListener)
        listeners.forEach { registerListener(it) }

        return commandListener
    }

    private fun registerPreconditions(): List<PreconditionData> {
        val preconditions = Reflections(globalPath, MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(Precondition::class.java)
            .map {
                val annotation = it.annotations.first { it.annotationClass == Precondition::class } as Precondition
                val condition = diService.invokeReturningMethod<(CommandEvent<*>) -> PreconditionResult>(it)

                PreconditionData(condition, annotation.priority)
            }

        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        return preconditions
    }

    private fun detectServices() {
        val services = Reflections(globalPath).getTypesAnnotatedWith(Service::class.java)
        diService.invokeDestructiveList(services)
    }

    private fun detectData() {
        val data = Reflections(globalPath).getTypesAnnotatedWith(Data::class.java)
        val missingData = diService.collectDataObjects(data)

        InternalLogger.startup(data.size.pluralize("Data"))

        if (missingData.isEmpty()) return

        val dataString = missingData.joinToString(", ", postfix = ".")

        InternalLogger.error("The below data files were generated and must be filled in before re-running.\n$dataString")

        exitProcess(0)
    }
}