@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.on
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
 */
data class ListenerBuilder(val discord: Discord) {
    /**
     * Create a new listener.
     */
    @InnerDSL
    inline fun <reified T : Event> on(crossinline listener: suspend T.() -> Unit) {
        discord.api.on<T> {
            listener.invoke(this)
        }
    }
}

/**
 * This is not for you...
 */
data class Listeners(private val collector: ListenerBuilder.() -> Unit) :BuilderRegister {
    override fun register(discord: Discord) {
        collector.invoke(ListenerBuilder(discord))
    }
}