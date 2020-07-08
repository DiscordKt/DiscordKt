package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.Member

/**
 * Accepts a Discord Member entity as an ID or mention.
 *
 * @param allowsBot Whether or not a bot is a valid input.
 */
open class MemberArg(override val name: String = "Member", private val allowsBot: Boolean = false) : ArgumentType<Member>() {
    companion object : MemberArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("$name cannot be accessed from outside a guild.")
        val id = arg.trimToID()

        val member = guild.getMemberById(id)
            ?: return Error("Couldn't retrieve $name from $arg.")

        if (!allowsBot && member.user.isBot)
            return Error("$name cannot be a bot.")

        return Success(member)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.author.id)
}