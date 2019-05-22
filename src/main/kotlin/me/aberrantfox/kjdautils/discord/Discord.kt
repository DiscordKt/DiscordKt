package me.aberrantfox.kjdautils.discord

import me.aberrantfox.kjdautils.api.dsl.KConfiguration

import net.dv8tion.jda.core.JDA

interface Discord {
  @Deprecated("To be removed")
  val jda: JDA

  companion object {
    fun build(configuration: KConfiguration): Discord = KJDA.build(configuration)
  }
}



