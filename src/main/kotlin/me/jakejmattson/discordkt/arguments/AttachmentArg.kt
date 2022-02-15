package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Attachment
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts all message attachments.
 */
public open class AttachmentArg(override val name: String = "Attachments",
                                override val description: String = internalLocale.attachmentArgDescription) : Argument<Set<Attachment>> {
    /**
     * Accepts all message attachments.
     */
    public companion object : AttachmentArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Set<Attachment>> {
        val attachments = event.message!!.attachments

        if (attachments.isEmpty())
            return Error("No attachments")

        return Success(attachments, 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("Attachments")
    public override fun formatData(data: Set<Attachment>): String = data.toString()
}