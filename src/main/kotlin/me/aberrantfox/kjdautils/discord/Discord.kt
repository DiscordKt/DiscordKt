package me.aberrantfox.kjdautils.discord

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.internal.event.EventRegister
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger

import net.dv8tion.jda.core.JDA

// TODO: Don't expose these JDA types via the API
import net.dv8tion.jda.core.entities.MessageEmbed

interface Discord {
  @Deprecated("To be removed")
  val jda: JDA

  fun addEventListener(er: EventRegister): Unit

  fun getUserById(userId: String): User?

  companion object {
    fun build(configuration: KConfiguration): Discord = KJDA.build(configuration)
  }
}

interface User {
  val isBot: Boolean

  fun sendPrivateMessage(msg: String, log: BotLogger = DefaultLogger())
  fun sendPrivateMessage(msg: MessageEmbed, log: BotLogger = DefaultLogger())
}

