package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.Guild

open class GuildArg(override val name: String = "Guild"): ArgumentType<Guild>() {
    companion object : GuildArg()

    override val consumptionType = ConsumptionType.Single

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Guild> {
        val guild = event.discord.jda.guilds.firstOrNull { it.id == arg.trimToID() }
            ?: return ArgumentResult.Error("Couldn't retrieve guild: $arg")

        return ArgumentResult.Success(guild)
    }

    override fun generateExamples(event: CommandEvent<*>) = mutableListOf(event.guild?.id ?: "244230771232079873")
}
