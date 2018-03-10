package me.aberrantfox.kjdautils.api

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.produceContainer
import me.aberrantfox.kjdautils.internal.listeners.CommandListener
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder


class KUtils(val config: KJDAConfiguration) {
    var container: CommandsContainer = produceContainer(config.commandPath)
    val jda = JDABuilder(AccountType.BOT).setToken(config.token).buildBlocking()
    var logger = DefaultLogger()

    private val listener = CommandListener(config, container, jda, logger)

    init {
        jda.addEventListener(listener)
    }

    fun registerCommandPrecondition(condition: (CommandEvent) -> Boolean) = listener.addPrecondition(condition)
}

fun startBot(token: String, prefix: String, commandPath: String, operate: KUtils.() -> Unit = {}): KUtils {
    val util = KUtils(KJDAConfiguration(token, prefix, commandPath))
    util.operate()
    return util
}