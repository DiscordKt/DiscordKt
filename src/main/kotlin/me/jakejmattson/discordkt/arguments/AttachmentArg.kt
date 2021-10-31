package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Attachment
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a file as a message attachment.
 */
public open class AttachmentArg(override val name: String = "Attachment",
                                override val description: String = internalLocale.attachmentArgDescription) : Argument<Attachment> {
    /**
     * Accepts a file as a message attachment.
     */
    public companion object : AttachmentArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Attachment> {
        val attachments = event.message!!.attachments

        if (attachments.isEmpty())
            return Error("No attachments")

        return Success(attachments.first(), 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("File")
    override fun formatData(data: Attachment): String = data.filename
}