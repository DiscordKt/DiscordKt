package me.aberrantfox.kjdautils.api

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.discord.*
import me.aberrantfox.kjdautils.extensions.stdlib.pluralize
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.event.EventRegister
import me.aberrantfox.kjdautils.internal.listeners.*
import me.aberrantfox.kjdautils.internal.services.*
import me.aberrantfox.kjdautils.internal.utils.InternalLogger
import me.aberrantfox.kjdautils.internal.utils.Validator
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.slf4j.impl.SimpleLogger
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()
inline fun <reified T> Discord.getInjectionObject() = diService.getElement(T::class.java) as T?

private var configured = false

class KUtils(private val config: KConfiguration, token: String, private val globalPath: String) {
    val discord = buildDiscordClient(config, token)
    private val conversationService: ConversationService = ConversationService(discord)

    init {
        InternalLogger.startup("--------------- KUtils Startup ---------------")
        InternalLogger.startup("GlobalPath: $globalPath")
        discord.addEventListener(EventRegister)
        registerInjectionObjects(discord, conversationService)
    }

    fun registerInjectionObjects(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun configure(setup: KConfiguration.() -> Unit = {}) {
        detectData()
        detectServices()

        val container = registerCommands()
        val preconditions = registerPreconditions().toMutableList()
        registerListeners(container, preconditions)

        conversationService.registerConversations(globalPath)

        createDocumentation(container)
        Validator.validateCommandMeta(container)
        Validator.validateReaction(config)

        configured = true
        config.setup()
    }

    private fun registerCommands(): CommandsContainer {
        val localContainer = produceContainer(globalPath, diService)

        //Add KUtils help command if a command named "Help" is not already provided
        val helpService = HelpService(localContainer, config)
        localContainer["Help"] ?: localContainer.join(helpService.produceHelpCommandContainer())

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

        val conversationListener = ConversationListener(conversationService)
        val commandListener = CommandListener(config, container, discord, CommandExecutor(), preconditions)

        registerListener(conversationListener)
        registerListener(commandListener)
        listeners.forEach { registerListener(it) }

        return commandListener
    }

    private fun registerPreconditions(): List<PreconditionData> {
        val preconditions = Reflections(globalPath, MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(Precondition::class.java)
            .map {
                val annotation = it.annotations.first { it.annotationClass == Precondition::class } as Precondition
                val condition = diService.invokeReturningMethod(it) as ((CommandEvent<*>) -> PreconditionResult)

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

        if(missingData.isEmpty()) return

        val dataString = missingData.joinToString(", ", postfix = ".")

        InternalLogger.error("The below data files were generated and must be filled in before re-running.\n$dataString")

        exitProcess(0)
    }
}

fun startBot(token: String, globalPath: String = defaultGlobalPath(Exception()), operate: KUtils.() -> Unit = {}): KUtils {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN")

    val util = KUtils(KConfiguration(), token, globalPath)
    util.operate()

    if(!configured) {
        util.configure()
    }

    InternalLogger.startup("----------------------------------------------")
    return util
}

private fun defaultGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    return full.substring(0, full.lastIndexOf("."))
}
