package me.jakejmattson.discordkt.api.arguments

import dev.kord.core.entity.User
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord User entity as an ID or mention.
 */
open class UserArg(override val name: String = "User") : ArgumentType<User> {
    /**
     * Accepts a Discord User entity as an ID or mention. Does not allow bots.
     */
    companion object : UserArg()

    override val description = "A Discord user"

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val user = arg.toSnowflakeOrNull()?.let { event.discord.kord.getUser(it) }
            ?: return Error(internalLocale.notFound)

        return Success(user)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(event.author.mention)
    override fun formatData(data: User) = "@${data.tag}"
}
