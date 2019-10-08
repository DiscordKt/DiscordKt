package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.Member

open class MemberArg(override val name: String = "Member"): ArgumentType<Member>() {
    companion object : MemberArg()

    override val examples = arrayListOf("@Bob", "133997975662886912", "215210079148834816")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val user = event.discord.jda.getUserById(arg.trimToID())
            ?: return ArgumentResult.Error("$arg does not share a common guild.")

        if (user.isBot) return ArgumentResult.Error("The target user cannot be a bot.")

        val member = user.toMember(event.guild!!)
            ?: return ArgumentResult.Error("The target user is not in this guild.")

        return ArgumentResult.Success(member)
    }
}