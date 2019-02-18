package me.aberrantfox.kjdautils.api

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.Data
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.di.DIService
import me.aberrantfox.kjdautils.internal.event.EventRegister
import me.aberrantfox.kjdautils.internal.listeners.CommandListener
import me.aberrantfox.kjdautils.internal.listeners.ConversationListener
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner


class KUtils(val config: KJDAConfiguration) {
    val jda = JDABuilder(AccountType.BOT).setToken(config.token).buildBlocking()

    private var listener: CommandListener? = null
    private var executor: CommandExecutor? = null
    private val helpService: HelpService
    private val diService = DIService()

    init {
        registerInjectionObject(jda)
    }

    val conversationService: ConversationService = ConversationService(jda, config, diService)
    val container = CommandsContainer()
    var logger: BotLogger = DefaultLogger()

    init {
        registerInjectionObject(conversationService)
        jda.addEventListener(EventRegister)
        helpService = HelpService(container, config)
        registerListeners(ConversationListener(conversationService))
    }

    fun registerInjectionObject(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun registerCommandPreconditions(vararg conditions: (CommandEvent) -> PreconditionResult) = listener?.addPreconditions(*conditions)
    
    fun configure(setup: KJDAConfiguration.() -> Unit) {
        config.setup()

        detectData()
        detectServices()

        registerCommands()
        registerListenersByPath()
        registerPreconditionsByPath()
        conversationService.registerConversations(config.globalPath)
    }

    fun registerListeners(vararg listeners: Any) = listeners.forEach { EventRegister.eventBus.register(it) }

    private fun registerCommands(): CommandsContainer {
        val localContainer = produceContainer(config.globalPath, diService)
        CommandRecommender.addAll(localContainer.listCommands())

        val executor = CommandExecutor()
        val listener = CommandListener(config, container, logger, executor)

        this.container.join(localContainer)
        this.executor = executor
        this.listener = listener

        registerListeners(listener)
        return container
    }

    private fun registerListenersByPath() {
        Reflections(config.globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Subscribe::class.java)
                .map { it.declaringClass }
                .distinct()
                .map { diService.invokeConstructor(it) }
                .forEach { registerListeners(it) }
    }

    private fun registerPreconditionsByPath() {
        Reflections(config.globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Precondition::class.java)
                .map { diService.invokeReturningMethod(it) as ((CommandEvent) -> PreconditionResult) }
                .forEach { registerCommandPreconditions(it) }
    }

    private fun detectServices() {
        val services = Reflections(config.globalPath).getTypesAnnotatedWith(Service::class.java)
        diService.invokeDestructiveList(services)
    }

    private fun detectData() {
        val data = Reflections(config.globalPath).getTypesAnnotatedWith(Data::class.java)
        diService.collectDataObjects(data)
    }
}

fun startBot(token: String, operate: KUtils.() -> Unit = {}): KUtils {
    val util = KUtils(KJDAConfiguration(token))
    util.operate()
    return util
}