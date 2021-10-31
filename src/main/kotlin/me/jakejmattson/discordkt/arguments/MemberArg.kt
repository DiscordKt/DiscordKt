package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Member
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Member entity as an ID or mention.
 *
 * @param allowsBot Whether a bot is a valid input.
 */
public open class MemberArg(override val name: String = "Member",
                            override val description: String = internalLocale.memberArgDescription,
                            private val allowsBot: Boolean = false) : Argument<Member> {
    /**
     * Accepts a Discord Member entity as an ID or mention. Does not allow bots.
     */
    public companion object : MemberArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("No guild found")

        val member = arg.toSnowflakeOrNull()?.let { guild.getMemberOrNull(it) }
            ?: return Error(internalLocale.notFound)

        if (!allowsBot && member.isBot)
            return Error("Cannot be a bot")

        return Success(member)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf(event.author.mention)
    override fun formatData(data: Member): String = "@${data.tag}"
}