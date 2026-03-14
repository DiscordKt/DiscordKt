package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
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
public open class MessageArg(
    override val name: String = "Message",
    override val description: String = internalLocale.messageArgDescription,
    private val allowsGlobal: Boolean = false
) : StringArgument<Message> {
    /**
     * Accepts a Discord Message entity as an ID or a link from within this guild.
     */
    public companion object : MessageArg()

    override suspend fun transform(input: String, context: DiscordContext): Either<String, Message> = either {
        val message = when {
            DiscordRegex.publicMessage.matches(input) -> {
                val (guildId, channelId, messageId) = input.split("/").takeLast(3).map { it.toSnowflakeOrNull() }

                ensure(allowsGlobal || guildId == context.guild?.id) {
                    "Must be from this guild"
                }

                val guild = ensureNotNull(guildId?.let { context.discord.kord.getGuild(it) }) {
                    "Invalid guild"
                }

                val channel = ensureNotNull(channelId?.let { guild.getChannelOfOrNull<GuildMessageChannel>(it) }) {
                    "Invalid channel"
                }

                ensureNotNull(messageId?.let { channel.getMessageOrNull(it) }) {
                    "Invalid message"
                }
            }

            DiscordRegex.privateMessage.matches(input) -> raise("Cannot resolve private URL - use message ID")
            else -> ensureNotNull(input.toSnowflakeOrNull()?.let { context.channel.getMessageOrNull(it) }) {
                "Invalid ID"
            }
        }

        message
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        context.message?.let { listOf(it.id.toString()) } ?: emptyList()
}