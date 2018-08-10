package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.tryRetrieveSnowflake

open class MessageArg(override val name: String = "MessageID") : ArgumentType {
    companion object : MessageArg()

    override val examples = arrayListOf("455099008013303819", "455099111327137807", "244099459327137807")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) {
            event.channel.getMessageById(arg.trimToID()).complete()
        }

        return if (retrieved != null) {
            ArgumentResult.Single(retrieved)
        } else {
            ArgumentResult.Error("Couldn't retrieve a message with the id given from this channel.")
        }
    }
}