package me.aberrantfox.kjdautils.discord

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.extensions.jda.*
import me.aberrantfox.kjdautils.internal.logging.BotLogger

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.User as JDAUser
import net.dv8tion.jda.core.entities.MessageEmbed

class KJDA(override val jda: JDA) : Discord {

  override fun getUserById(userId: String): User {
    return jda.getUserById(userId).fromJDA()
  }

  companion object {
    fun build(config: KConfiguration) =
      KJDA(JDABuilder(AccountType.BOT).setToken(config.token).buildBlocking())
  }
}

fun JDAUser.fromJDA(): User { return KJDAUser(this) }

class KJDAUser(private val jdaUser: JDAUser) : User {
  override val isBot = jdaUser.isBot

  override fun sendPrivateMessage(msg: String, log: BotLogger) {
    jdaUser.sendPrivateMessage(msg, log)
  }

  override fun sendPrivateMessage(msg: MessageEmbed, log: BotLogger) {
    jdaUser.sendPrivateMessage(msg, log)
  }
}
