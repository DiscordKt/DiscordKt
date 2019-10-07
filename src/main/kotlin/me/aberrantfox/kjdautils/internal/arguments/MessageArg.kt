package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.Message

open class MessageArg(override val name: String = "MessageID"): ArgumentType<Message>() {
    companion object : MessageArg()

    override val examples = arrayListOf("455099008013303819", "455099111327137807", "244099459327137807")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Message> {
        val retrieved = event.channel.retrieveMessageById(arg.trimToID()).complete()
            ?: return ArgumentResult.Error("Couldn't retrieve a message with the id given from this channel.")

        return ArgumentResult.Success(retrieved)
    }
}