@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.utils.BuilderRegister

/**
 * Create a block for registering preconditions.
 *
 * @param construct The builder function.
 */
@BuilderDSL
public fun precondition(priority: Int = 5, construct: suspend PreconditionBuilder.() -> Unit): Precondition = Precondition(priority, construct)

/**
 * @suppress Used in DSL
 */
public data class PreconditionBuilder(private val event: CommandEvent<*>) : CommandEvent<TypeContainer>(event.discord, event.message, event.author, event.channel, event.guild) {
    /**
     * Fail this precondition.
     *
     * @param reason The reason for failure.
     */
    public fun fail(reason: String = "") {
        throw Exception(reason)
    }
}

/**
 * This is not for you...
 */
public class Precondition(internal val priority: Int, private val construct: suspend PreconditionBuilder.() -> Unit) : BuilderRegister {
    internal suspend fun check(event: CommandEvent<*>) = construct.invoke(PreconditionBuilder(event))

    /** @suppress */
    override fun register(discord: Discord) {
        discord.preconditions.add(this)
    }
}