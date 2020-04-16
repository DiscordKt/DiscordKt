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
import me.aberrantfox.kjdautils.internal.logging.*
import me.aberrantfox.kjdautils.internal.services.*
import me.aberrantfox.kjdautils.internal.utils.InternalLogger
import me.aberrantfox.kjdautils.internal.utils.Validator
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()
inline fun <reified T> Discord.getInjectionObject() = diService.getElement(T::class.java) as T?

private var configured = false

class KUtils(val config: KConfiguration, token: String) {
    val discord = buildDiscordClient(config, token)
    private val conversationService: ConversationService = ConversationService(discord, diService)

    init {
        println("--------------- KUtils Startup ---------------")
        discord.addEventListener(EventRegister)
        registerInjectionObjects(discord, conversationService)
    }

    fun registerInjectionObjects(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun configure(setup: KConfiguration.() -> Unit = {}) {
        configured = true
        config.setup()

        detectData()
        detectServices()

        val container = registerCommands()
        val commandListener = registerListeners(container)
        registerPreconditions(commandListener)
        conversationService.registerConversations(config.globalPath)

        val documentationService = DocumentationService(container)
        documentationService.generateDocumentation(config.documentationSortOrder)
    }

    private fun registerCommands(): CommandsContainer {
        val localContainer = produceContainer(config.globalPath, diService)

        //Add KUtils help command if a command named "Help" is not already provided
        val helpService = HelpService(localContainer, config)
        localContainer["Help"] ?: localContainer.join(helpService.produceHelpCommandContainer())

        CommandRecommender.addAll(localContainer.commands)
        Validator.validateCommandConsumption(localContainer)

        return localContainer
    }

    private fun registerListeners(container: CommandsContainer): CommandListener {
        val listeners = Reflections(config.globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Subscribe::class.java)
            .map { it.declaringClass }
            .distinct()
            .map { diService.invokeConstructor(it) }

        InternalLogger.startup(listeners.size.pluralize("Listener"))

        fun registerListener(listener: Any) = EventRegister.eventBus.register(listener)

        val conversationListener = ConversationListener(conversationService)
        val commandListener = CommandListener(config, container, DefaultLogger(), discord, CommandExecutor())

        registerListener(conversationListener)
        registerListener(commandListener)
        listeners.forEach { registerListener(it) }

        return commandListener
    }

    private fun registerPreconditions(commandListener: CommandListener) {
        val preconditions = Reflections(config.globalPath, MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(Precondition::class.java)
            .map {
                val annotation = it.annotations.first { it.annotationClass == Precondition::class } as Precondition
                val condition = diService.invokeReturningMethod(it) as ((CommandEvent<*>) -> PreconditionResult)

                PreconditionData(condition, annotation.priority)
            }

        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        preconditions.forEach { commandListener.addPreconditions(it) }
    }

    private fun detectServices() {
        val services = Reflections(config.globalPath).getTypesAnnotatedWith(Service::class.java)
        diService.invokeDestructiveList(services)
    }

    private fun detectData() {
        val data = Reflections(config.globalPath).getTypesAnnotatedWith(Data::class.java)
        val missingData = diService.collectDataObjects(data)

        InternalLogger.startup(data.size.pluralize("Data"))

        if(missingData.isEmpty()) return

        val dataString = missingData.joinToString(", ", postfix = ".")

        InternalLogger.error("The below data files were generated and must be filled in before re-running.\n$dataString")

        exitProcess(0)
    }
}

fun startBot(token: String, operate: KUtils.() -> Unit = {}): KUtils {
    val util = KUtils(KConfiguration(), token)
    util.config.globalPath = defaultGlobalPath(Exception())

    InternalLogger.startup("GlobalPath: ${util.config.globalPath}")

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
