package me.jakejmattson.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent
import me.aberrantfox.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.Member

open class MemberArg(override val name: String = "Member", private val allowsBot: Boolean = false) : ArgumentType<Member>() {
    companion object : MemberArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return ArgumentResult.Error("Member's can only belong to guilds.")
        val id = arg.trimToID()

        val member = guild.getMemberById(id)
            ?: return ArgumentResult.Error("Could not find a member in this guild with ID $id")

        if (!allowsBot && member.user.isBot)
            return ArgumentResult.Error("A bot is not a valid member arg.")

        return ArgumentResult.Success(member)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.author.id)
}