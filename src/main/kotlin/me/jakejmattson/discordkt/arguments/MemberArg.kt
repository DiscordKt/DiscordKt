package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a Discord Member entity as an ID or mention.
 *
 * @param allowsBot Whether a bot is a valid input.
 */
public open class MemberArg(override val name: String = "Member",
                            override val description: String = internalLocale.memberArgDescription,
                            private val allowsBot: Boolean = false) : UserArgument<Member> {
    /**
     * Accepts a Discord Member entity as an ID or mention. Does not allow bots.
     */
    public companion object : MemberArg()

    override suspend fun transform(input: User, context: DiscordContext): Result<Member> {
        val guild = context.guild ?: return Error("No guild found")
        val member = input.asMemberOrNull(guild.id) ?: return Error(internalLocale.notFound)

        if (!allowsBot && member.isBot)
            return Error("Cannot be a bot")

        return Success(member)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.author.mention)
}