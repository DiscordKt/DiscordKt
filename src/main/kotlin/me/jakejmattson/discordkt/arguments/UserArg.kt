package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.User
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a Discord User entity as an ID or mention.
 */
public open class UserArg(override val name: String = "User",
                          override val description: String = internalLocale.userArgDescription) : UserArgument<User> {
    /**
     * Accepts a Discord User entity as an ID or mention. Does not allow bots.
     */
    public companion object : UserArg()

    override suspend fun transform(input: User, context: DiscordContext): Result<User> = Success(input)
}