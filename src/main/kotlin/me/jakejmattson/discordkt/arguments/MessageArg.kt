package me.jakejmattson.discordkt.arguments

import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.DiscordRegex
import me.jakejmattson.discordkt.util.toSnowflakeOrNull

/**
 * Accepts a Discord Message entity as an ID or a link.
 *
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
public open class MessageArg(override val name: String = "Message",
                             override val description: String = internalLocale.messageArgDescription,
                             private val allowsGlobal: Boolean = false) : StringArgument<Message> {
    /**
     * Accepts a Discord Message entity as an ID or a link from within this guild.
     */
    public companion object : MessageArg()

    override suspend fun transform(input: String, context: DiscordContext): Result<Message> {
        val message = when {
            DiscordRegex.publicMessage.matches(input) -> {
                val (guildId, channelId, messageId) = input.split("/").takeLast(3).map { it.toSnowflakeOrNull() }

                if (!allowsGlobal && guildId != context.guild?.id)
                    return Error("Must be from this guild")

                val guild = guildId?.let { context.discord.kord.getGuildOrNull(it) } ?: return Error("Invalid guild")

                val channel = channelId?.let { guild.getChannelOfOrNull<GuildMessageChannel>(it) }
                    ?: return Error("Invalid channel")

                messageId?.let { channel.getMessageOrNull(it) } ?: return Error("Invalid message")
            }

            DiscordRegex.privateMessage.matches(input) -> return Error("Cannot resolve private URL - use message ID")
            else -> input.toSnowflakeOrNull()?.let { context.channel.getMessageOrNull(it) }
                ?: return Error("Invalid ID")
        }

        return Success(message)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = context.message?.let { listOf(it.id.toString()) }
        ?: emptyList()
}