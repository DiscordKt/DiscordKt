package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.jda.*
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.Member

/**
 * Accepts a Discord Member entity as an ID or mention.
 *
 * @param allowsBot Whether or not a bot is a valid input.
 */
open class MemberArg(override val name: String = "Member", private val allowsBot: Boolean = false) : ArgumentType<Member>() {
    /**
     * Accepts a Discord Member entity as an ID or mention. Does not allow bots.
     */
    companion object : MemberArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("No guild found")
        val id = arg.trimToID()

        val member = guild.jda.tryRetrieveSnowflake {
            guild.getMemberById(id)
        } as Member? ?: return Error("Not found")

        if (!allowsBot && member.user.isBot)
            return Error("Cannot be a bot")

        return Success(member)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.author.id)
    override fun formatData(data: Member) = "@${data.user.fullName()}"
}