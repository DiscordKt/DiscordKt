@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.*

/** @suppress Redundant doc */
interface PreconditionResult

/**
 * Object indicating that this precondition has passed.
 */
object Pass : PreconditionResult

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
    fun evaluate(priority: Int = 5, condition: suspend CommandEvent<*>.() -> PreconditionResult) {
        discord.preconditions.add(Precondition(priority, condition))
    }
}

/**
 * This is not for you...
 */
data class Preconditions(private val collector: PreconditionBuilder.() -> Unit) {
    internal fun register(discord: Discord) {
        val preconditionBuilder = PreconditionBuilder(discord)
        collector.invoke(preconditionBuilder)
    }
}

/**
 * This is not for you...
 */
data class Precondition(val priority: Int, private val construct: suspend CommandEvent<*>.() -> PreconditionResult) {
    internal suspend fun evaluate(event: CommandEvent<*>) = construct.invoke(event)
}