package me.aberrantfox.kjdautils.discord

import com.google.gson.Gson
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.internal.event.EventRegister
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed

data class KUtilsProperties(val kutilsVersion: String, val kotlinVersion: String, val jdaVersion: String, val repository: String)
private val propFile = KUtilsProperties::class.java.getResource("/kutils-properties.json").readText()

abstract class Discord {
    @Deprecated("To be removed")
    abstract val jda: JDA
    abstract val configuration: KConfiguration
    val properties = Gson().fromJson(propFile, KUtilsProperties::class.java)

    abstract fun addEventListener(er: EventRegister)

    abstract fun getUserById(userId: String): User?
}

interface User {
    val isBot: Boolean

    fun sendPrivateMessage(msg: String)
    fun sendPrivateMessage(msg: MessageEmbed)
}

fun buildDiscordClient(configuration: KConfiguration, token: String): Discord = KJDA.build(configuration, token)

