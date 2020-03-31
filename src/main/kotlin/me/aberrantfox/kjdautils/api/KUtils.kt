package me.aberrantfox.kjdautils.api

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.discord.*
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.event.EventRegister
import me.aberrantfox.kjdautils.internal.listeners.*
import me.aberrantfox.kjdautils.internal.logging.*
import me.aberrantfox.kjdautils.internal.services.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import kotlin.system.exitProcess

@PublishedApi
internal val diService = DIService()
inline fun <reified T> Discord.getInjectionObject() = diService.getElement(T::class.java) as T?

class KUtils(val config: KConfiguration, token: String) {
    val discord = buildDiscordClient(config, token)

    private var listener: CommandListener? = null
    private var executor: CommandExecutor? = null
    private val documentationService: DocumentationService
    var configured = false

    init {
        registerInjectionObject(discord)
    }

    val conversationService: ConversationService = ConversationService(discord, diService)
    val container = CommandsContainer()
    var logger: BotLogger = DefaultLogger()

    init {
        registerInjectionObject(conversationService)
        discord.addEventListener(EventRegister)
        documentationService = DocumentationService(container)
        registerListeners(ConversationListener(conversationService))
    }

    fun registerInjectionObject(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun registerCommandPreconditions(vararg conditions: (CommandEvent<*>) -> PreconditionResult) =
            conditions.map { PreconditionData(it) }
                      .forEach { listener?.addPreconditions(it) }

    fun registerCommandPreconditions(vararg preconditions: PreconditionData) =
            listener?.addPreconditions(*preconditions)

    fun configure(setup: KConfiguration.() -> Unit = {}) {
        configured = true
        config.setup()

        detectData()
        detectServices()

        registerCommands()
        registerListenersByPath()
        registerPreconditionsByPath()
        conversationService.registerConversations(config.globalPath)
        documentationService.generateDocumentation(config.documentationSortOrder)
    }

    fun registerListeners(vararg listeners: Any) = listeners.forEach { EventRegister.eventBus.register(it) }

    private fun registerCommands(): CommandsContainer {
        val localContainer = produceContainer(config.globalPath, diService)

        //Add KUtils help command if a command named "Help" is not already provided
        val helpService = HelpService(container, config)
        localContainer["Help"] ?: localContainer.join(helpService.produceHelpCommandContainer())

        CommandRecommender.addAll(localContainer.commands)

        validateCommandConsumption(localContainer)

        val executor = CommandExecutor()
        val listener = CommandListener(config, container, logger, discord, executor)

        this.container.join(localContainer)
        this.executor = executor
        this.listener = listener

        registerListeners(listener)
        return container
    }

    private fun validateCommandConsumption(commandsContainer: CommandsContainer) {
        commandsContainer.commands.forEach { command ->
            val consumptionTypes = command.expectedArgs.arguments.map { it.consumptionType }

            if (!consumptionTypes.contains(ConsumptionType.All))
                return@forEach

            val allIndex = consumptionTypes.indexOfFirst { it == ConsumptionType.All }
            val lastIndex = consumptionTypes.lastIndex

            if (allIndex == lastIndex)
                return@forEach

            val remainingConsumptionTypes = consumptionTypes.subList(allIndex + 1, lastIndex + 1)

            remainingConsumptionTypes.takeWhile {
                if (it != ConsumptionType.None) {
                    InternalLogger.error("Detected ConsumptionType.$it after ConsumptionType.All in command: ${command.names.first()}")
                    false
                } else true
            }
        }
    }

    private fun registerListenersByPath() {
        val listeners = Reflections(config.globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Subscribe::class.java)
            .map { it.declaringClass }
            .distinct()
            .map { diService.invokeConstructor(it) }

        InternalLogger.info("Detected ${listeners.size} listeners.")

        listeners.forEach { registerListeners(it) }
    }

    private fun registerPreconditionsByPath() {
        val preconditions = Reflections(config.globalPath, MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(Precondition::class.java)
            .map {
                val preconditionAnnotation = it.annotations.first { annotation -> annotation.annotationClass == Precondition::class }
                val priority = (preconditionAnnotation as Precondition).priority
                val condition = diService.invokeReturningMethod(it) as ((CommandEvent<*>) -> PreconditionResult)

                PreconditionData(condition, priority)
            }

        InternalLogger.info("Detected ${preconditions.size} preconditions.")

        preconditions.forEach { registerCommandPreconditions(it) }
    }

    private fun detectServices() {
        val services = Reflections(config.globalPath).getTypesAnnotatedWith(Service::class.java)
        diService.invokeDestructiveList(services)
    }

    private fun detectData() {
        val data = Reflections(config.globalPath).getTypesAnnotatedWith(Data::class.java)
        val fillInData = diService.collectDataObjects(data)

        exitIfDataNeedsToBeFilledIn(fillInData)
    }

    private fun exitIfDataNeedsToBeFilledIn(data: ArrayList<String>) {
        if(data.isEmpty()) return

        val dataString = data.joinToString(", ", postfix = ".")

        println("The below data files were generated and must be filled in before re-running.")
        println(dataString)
        exitProcess(0)
    }
}

fun startBot(token: String, operate: KUtils.() -> Unit = {}): KUtils {
    val util = KUtils(KConfiguration(), token)
    util.config.globalPath = defaultGlobalPath(Exception())
    util.operate()

    if(!util.configured) {
        util.configure()
    }

    InternalLogger.info("GlobalPath set to ${util.config.globalPath}")
    return util
}

private fun defaultGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    return full.substring(0, full.lastIndexOf("."))
}
