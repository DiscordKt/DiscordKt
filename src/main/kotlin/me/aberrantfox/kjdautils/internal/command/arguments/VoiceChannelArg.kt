package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.tryRetrieveSnowflake

open class VoiceChannelArg(override val name : String = "The ID of any valid voice channel.") : ArgumentType {
    companion object : VoiceChannelArg()

    override val examples = arrayListOf("360583945982836746", "360729317631721482")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) { it.getVoiceChannelById(arg.trimToID()) }

        return if (retrieved != null) {
            ArgumentResult.Single(retrieved)
        } else {
            ArgumentResult.Error("Couldn't retrieve user: $arg")
        }
    }
}