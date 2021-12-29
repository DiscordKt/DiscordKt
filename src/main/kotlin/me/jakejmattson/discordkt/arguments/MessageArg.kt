package me.jakejmattson.discordkt.arguments

import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Message entity as an ID or a link.
 *
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
public open class MessageArg(override val name: String = "Message",
                             override val description: String = internalLocale.messageArgDescription,
                             private val allowsGlobal: Boolean = false) : Argument<Message> {
    /**
     * Accepts a Discord Message entity as an ID or a link from within this guild.
     */
    public companion object : MessageArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Message> {
        val publicRegex = "https://discord(app)?.com/channels/\\d+/\\d+/\\d+".toRegex()
        val privateRegex = "https://discord(app)?.com/channels/@me/\\d+/\\d+".toRegex()

        val message = when {
            publicRegex.matches(arg) -> {
                val (guildId, channelId, messageId) = arg.split("/").takeLast(3).map { it.toSnowflakeOrNull() }

                if (!allowsGlobal && guildId != event.guild?.id)
                    return Error("Must be from this guild")

                val guild = guildId?.let { event.discord.kord.getGuild(it) } ?: return Error("Invalid guild")

                val channel = channelId?.let { guild.getChannelOfOrNull<GuildMessageChannel>(it) }
                    ?: return Error("Invalid channel")

                messageId?.let { channel.getMessageOrNull(it) } ?: return Error("Invalid message")
            }
            privateRegex.matches(arg) -> return Error("Cannot resolve private URL - use message ID")
            else -> arg.toSnowflakeOrNull()?.let { event.channel.getMessageOrNull(it) } ?: return Error("Invalid ID")
        }

        return Success(message)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf(event.message!!.id.toString())
}