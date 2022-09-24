@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.Command
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
public data class PreconditionBuilder(private val event: CommandEvent<*>) : CommandEvent<TypeContainer> {
    override val command: Command = event.command
    override val discord: Discord = event.discord
    override val message: Message? = event.message
    override val author: User = event.author
    override val channel: MessageChannel = event.channel
    override val guild: Guild? = event.guild
    override val args: TypeContainer = event.args
    override val interaction: ApplicationCommandInteraction? = event.interaction

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