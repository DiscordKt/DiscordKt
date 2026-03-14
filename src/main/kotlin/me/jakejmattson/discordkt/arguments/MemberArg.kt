package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a Discord Member entity as an ID or mention.
 *
 * @param allowsBot Whether a bot is a valid input.
 */
public open class MemberArg(
    override val name: String = "Member",
    override val description: String = internalLocale.memberArgDescription,
    private val allowsBot: Boolean = false
) : UserArgument<Member> {
    /**
     * Accepts a Discord Member entity as an ID or mention. Does not allow bots.
     */
    public companion object : MemberArg()

    override suspend fun transform(input: User, context: DiscordContext): Either<String, Member> = either {
        val guild = ensureNotNull(context.guild) {
            "No guild found"
        }

        val member = ensureNotNull(input.asMemberOrNull(guild.id)) {
            internalLocale.notFound
        }

        ensure(allowsBot || !member.isBot) {
            "Cannot be a bot"
        }

        member
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.author.mention)
}