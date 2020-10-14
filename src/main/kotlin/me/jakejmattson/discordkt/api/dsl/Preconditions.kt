@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.utils.BuilderRegister

/**
 * Create a block for registering preconditions.
 *
 * @param construct The builder function.
 */
@BuilderDSL
fun precondition(priority: Int = 5, construct: PreconditionBuilder.() -> Unit) = Precondition(priority, construct)

/**
 * @suppress Used in DSL
 */
data class PreconditionBuilder(private val event: CommandEvent<*>) : CommandEvent<GenericContainer>(event.rawInputs, event.discord, event.message, event.author, event.channel, event.guild) {
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
data class Precondition(val priority: Int, private val construct: PreconditionBuilder.() -> Unit) : BuilderRegister {
    internal fun check(event: CommandEvent<*>) = construct.invoke(PreconditionBuilder(event))
    override fun register(discord: Discord) {
        discord.preconditions.add(this)
    }
}