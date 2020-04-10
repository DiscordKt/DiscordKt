package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.*

open class VoiceChannelArg(override val name : String = "The ID of any valid voice channel.") : ArgumentType<VoiceChannel>() {
    companion object : VoiceChannelArg()

    override val consumptionType = ConsumptionType.Single
    override var exampleFactory = createExampleFactory {
        val channel = it.guild?.channels?.firstOrNull { it.type == ChannelType.VOICE } as? VoiceChannel
        mutableListOf(channel?.id ?: "582168201979494421")
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<VoiceChannel> {
        val channel = event.discord.jda.getVoiceChannelById(arg.trimToID())
            ?: return ArgumentResult.Error("Couldn't retrieve voice channel: $arg")

        return ArgumentResult.Success(channel)
    }
}