package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import net.dv8tion.jda.api.entities.Guild

open class GuildArg(override val name: String = "Guild"): ArgumentType<Guild> {
    companion object : GuildArg()

    override val examples = arrayListOf("244230771232079873")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Guild> {
        val guild = event.discord.jda.guilds.firstOrNull { it.id == arg.trimToID() }
            ?: return ArgumentResult.Error("Couldn't retrieve guild: $arg")

        return ArgumentResult.Success(guild)
    }
}
