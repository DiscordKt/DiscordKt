package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Attachment
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a message attachment.
 */
public open class AttachmentArg(override val name: String = "Attachment",
                                override val description: String = internalLocale.attachmentArgDescription) : AttachmentArgument<Attachment> {
    /**
     * Accepts a message attachment.
     */
    public companion object : AttachmentArg()

    override suspend fun transform(input: Attachment, context: DiscordContext): Result<Attachment> = Success(input)

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("Attachment")
}