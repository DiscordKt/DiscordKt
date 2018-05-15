package me.aberrantfox.kjdautils.api

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.di.DIService
import me.aberrantfox.kjdautils.internal.event.EventRegister
import me.aberrantfox.kjdautils.internal.listeners.CommandListener
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder


class KUtils(val config: KJDAConfiguration) {
    private var listener: CommandListener? = null
    private var container: CommandsContainer? = null
    private val diService = DIService()

    val jda = JDABuilder(AccountType.BOT).setToken(config.token).buildBlocking()
    var logger = DefaultLogger()

    init {
        jda.addEventListener(EventRegister)
    }

    fun registerInjectionObject(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun registerCommands(commandPath: String, prefix: String) {
        config.commandPath = commandPath
        config.prefix = prefix
        container = produceContainer(commandPath, diService)
        listener = CommandListener(config, container!!, jda, logger)
        registerListener(listener!!)
    }

    fun registerCommandPrecondition(condition: (CommandEvent) -> Boolean) = listener?.addPrecondition(condition)

    fun registerListener(listener: Any) = EventRegister.eventBus.register(listener)
}

fun startBot(token: String, operate: KUtils.() -> Unit = {}): KUtils {
    val util = KUtils(KJDAConfiguration(token))
    util.operate()
    return util
}