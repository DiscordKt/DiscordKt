package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.Member
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

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

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("No guild found")

        val member = arg.toSnowflakeOrNull()?.let { guild.getMemberOrNull(it) } ?: return Error("Not found")

        if (!allowsBot && member.isBot == true)
            return Error("Cannot be a bot")

        return Success(member)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.author.mention)
    override fun formatData(data: Member) = "@${data.tag}"
}