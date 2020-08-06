package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.jda.fullName
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.User

/**
 * Accepts a Discord User entity as an ID or mention.
 *
 * @param allowsBot Whether or not a bot is a valid input.
 */
open class UserArg(override val name: String = "User", private val allowsBot: Boolean = false) : ArgumentType<User>() {
    /**
     * Accepts a Discord User entity as an ID or mention. Does not allow bots.
     */
    companion object : UserArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val user = event.discord.retrieveEntity {
            it.retrieveUserById(arg.trimToID()).complete()
        } ?: return Error("Not found")

        if (!allowsBot && user.isBot)
            return Error("Cannot be a bot")

        return Success(user)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.author.id)
    override fun formatData(data: User) = "@${data.fullName()}"
}
