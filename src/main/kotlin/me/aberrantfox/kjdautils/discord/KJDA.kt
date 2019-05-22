package me.aberrantfox.kjdautils.discord

import me.aberrantfox.kjdautils.api.dsl.KConfiguration

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder

class KJDA(override val jda: JDA) : Discord {

  companion object {
    fun build(config: KConfiguration) =
      KJDA(JDABuilder(AccountType.BOT).setToken(config.token).buildBlocking())
  }
}

