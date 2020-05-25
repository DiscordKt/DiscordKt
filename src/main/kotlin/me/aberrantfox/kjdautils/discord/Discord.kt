package me.aberrantfox.kjdautils.discord

import com.google.gson.Gson
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.internal.event.EventRegister
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener

data class KUtilsProperties(val kutilsVersion: String, val kotlinVersion: String, val jdaVersion: String, val repository: String)
private val propFile = KUtilsProperties::class.java.getResource("/kutils-properties.json").readText()

abstract class Discord {
    @Deprecated("To be removed")
    abstract val jda: JDA
    abstract val configuration: KConfiguration
    val properties = Gson().fromJson(propFile, KUtilsProperties::class.java)

    abstract fun addEventListener(register: EventRegister)
}

fun buildDiscordClient(token: String, configuration: KConfiguration): Discord {
    val jda = JDABuilder(token).build()
    jda.awaitReady()
    return object : Discord() {
        override val jda: JDA = jda
        override val configuration: KConfiguration = configuration

        override fun addEventListener(register: EventRegister) {
            jda.addEventListener(object : EventListener {
                override fun onEvent(event: GenericEvent) = register.onEvent(event)
            })
        }
    }
}