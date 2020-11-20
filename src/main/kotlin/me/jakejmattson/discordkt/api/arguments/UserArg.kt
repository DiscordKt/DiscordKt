package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.User
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord User entity as an ID or mention.
 */
open class UserArg(override val name: String = "User") : ArgumentType<User>() {
    /**
     * Accepts a Discord User entity as an ID or mention. Does not allow bots.
     */
    companion object : UserArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val user = arg.toSnowflakeOrNull()?.let { event.discord.api.getUser(it) } ?: return Error("Not found")

        return Success(user)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(event.author.mention)
    override fun formatData(data: User) = "@${data.tag}"
}
