@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import dev.kord.core.event.Event
import dev.kord.core.on
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.*
import me.jakejmattson.discordkt.internal.utils.BuilderRegister

/**
 * Create a block for registering listeners.
 *
 * @param construct The builder function.
 */
@BuilderDSL
fun listeners(construct: ListenerBuilder.() -> Unit) = Listeners(construct)

/**
 * @suppress Used in DSL
 *
 * @param discord The discord instance.
 */
data class ListenerBuilder(val discord: Discord) {
    /**
     * Create a new listener.
     */
    @InnerDSL
    inline fun <reified T : Event> on(crossinline listener: suspend T.() -> Unit) {
        discord.configuration.enableEvent<T>()

        discord.api.on<T> {
            listener(this)
        }
    }
}

/**
 * This is not for you...
 */
data class Listeners(private val collector: ListenerBuilder.() -> Unit) : BuilderRegister {
    /** @suppress */
    override fun register(discord: Discord) {
        collector.invoke(ListenerBuilder(discord))
    }
}