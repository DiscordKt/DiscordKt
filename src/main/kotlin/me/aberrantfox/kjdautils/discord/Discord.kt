package me.aberrantfox.kjdautils.discord

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.internal.event.EventRegister
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed

abstract class Discord {
    @Deprecated("To be removed")
    abstract val jda: JDA
    abstract val configuration: KConfiguration

    abstract fun addEventListener(er: EventRegister)

    abstract fun getUserById(userId: String): User?
}

interface User {
    val isBot: Boolean

    fun sendPrivateMessage(msg: String)
    fun sendPrivateMessage(msg: MessageEmbed)
}

fun buildDiscordClient(configuration: KConfiguration, token: String): Discord = KJDA.build(configuration, token)

