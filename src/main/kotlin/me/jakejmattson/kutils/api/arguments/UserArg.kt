package me.jakejmattson.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent
import me.aberrantfox.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.User

open class UserArg(override val name: String = "User", private val allowsBot: Boolean = false) : ArgumentType<User>() {
    companion object : UserArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val user = tryRetrieveSnowflake(event.discord.jda) {
            it.retrieveUserById(arg.trimToID()).complete()
        } as User? ?: return ArgumentResult.Error("Couldn't retrieve user: $arg")

        if (!allowsBot && user.isBot)
            return ArgumentResult.Error("A bot is not a valid user arg.")

        return ArgumentResult.Success(user)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.author.id)
}
