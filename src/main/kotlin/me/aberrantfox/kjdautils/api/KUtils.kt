package me.aberrantfox.kjdautils.api

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.types.GuildID
import me.aberrantfox.kjdautils.internal.command.produceContainer
import me.aberrantfox.kjdautils.internal.listeners.CommandListener
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder


class KUtils(val config: KJDAConfiguration) {
    operator fun invoke(args: KJDAConfiguration.() -> Unit) {}

    val container: CommandsContainer = produceContainer(config.commandPath)
    val jda = JDABuilder(AccountType.BOT).setToken(config.token).buildBlocking()
    val guild = jda.getGuildById(config.guildID)

    var logger = DefaultLogger()

    private val listener = CommandListener(config, container, jda, logger, guild)

    init {
        jda.addEventListener(listener)
    }

    fun registerCommandPrecondition(condition: (CommandEvent) -> Boolean) = listener.addPrecondition(condition)
}

fun startBot(token: String, ownerID: String, prefix: String, guildID: GuildID, path: String, operate: KUtils.() -> Unit): KUtils {
    val util = KUtils(KJDAConfiguration(token, ownerID, prefix, guildID))
    util.operate()
    return util
}