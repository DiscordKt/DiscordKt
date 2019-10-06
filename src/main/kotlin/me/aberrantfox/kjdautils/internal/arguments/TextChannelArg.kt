package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import net.dv8tion.jda.api.entities.TextChannel

open class TextChannelArg(override val name : String = "TextChannel"): ArgumentType<TextChannel> {
    companion object : TextChannelArg()

    override val examples = arrayListOf("#chat", "4421069003953932345", "#test-channel", "292106900395393024")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<TextChannel> {
        val channel = event.discord.jda.getTextChannelById(arg.trimToID())
            ?: return ArgumentResult.Error("Couldn't retrieve text channel: $arg")

        return ArgumentResult.Success(channel)
    }
}