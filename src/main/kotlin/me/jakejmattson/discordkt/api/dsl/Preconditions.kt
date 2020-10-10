@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.*
import me.jakejmattson.discordkt.internal.utils.BuilderRegister

/**
 * Create a block for registering preconditions.
 *
 * @param construct The builder function.
 */
@BuilderDSL
fun preconditions(construct: PreconditionBuilder.() -> Unit) = Preconditions(construct)

/**
 * @suppress Used in DSL
 */
data class PreconditionBuilder(val discord: Discord) {
    /**
     * Create a new precondition.
     */
    @InnerDSL
    fun check(priority: Int = 5, condition: suspend CommandEvent<*>.() -> Unit) {
        discord.preconditions.add(Precondition(priority, condition))
    }

    /**
     * Fail this precondition.
     *
     * @param reason The reason for failure.
     */
    fun fail(reason: String = "") {
        throw Exception(reason)
    }
}

/**
 * This is not for you...
 */
data class Preconditions(private val collector: PreconditionBuilder.() -> Unit) : BuilderRegister {
    override fun register(discord: Discord) {
        val preconditionBuilder = PreconditionBuilder(discord)
        collector.invoke(preconditionBuilder)
    }
}

/**
 * This is not for you...
 */
data class Precondition(val priority: Int, private val construct: suspend CommandEvent<*>.() -> Unit) {
    internal suspend fun check(event: CommandEvent<*>) = construct.invoke(event)
}