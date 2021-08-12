package me.jakejmattson.discordkt.api.arguments

import dev.kord.core.entity.Member
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Member entity as an ID or mention.
 *
 * @param allowsBot Whether or not a bot is a valid input.
 */
open class MemberArg(override val name: String = "Member",
                     override val description: String = internalLocale.memberArgDescription,
                     private val allowsBot: Boolean = false) : Argument<Member> {
    /**
     * Accepts a Discord Member entity as an ID or mention. Does not allow bots.
     */
    companion object : MemberArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("No guild found")

        val member = arg.toSnowflakeOrNull()?.let { guild.getMemberOrNull(it) }
            ?: return Error(internalLocale.notFound)

        if (!allowsBot && member.isBot)
            return Error("Cannot be a bot")

        return Success(member)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(event.author.mention)
    override fun formatData(data: Member) = "@${data.tag}"
}