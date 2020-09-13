package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.firstOrNull
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflake

/**
 * Accepts a Discord Message entity as an ID or a link.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class MessageArg(override val name: String = "Message", private val allowsGlobal: Boolean = false) : ArgumentType<Message>() {
    /**
     * Accepts a Discord Message entity as an ID or a link from within this guild.
     */
    companion object : MessageArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Message> {
        val regex = "https://discordapp.com/channels/\\d+/\\d+/\\d+".toRegex()
        val isLink = regex.matches(arg)
        val kord = event.discord.api

        val message = if (isLink) {
            val (guildId, channelId, messageId) = arg.split("/").takeLast(3).map { it.toSnowflake() }

            if (!allowsGlobal && guildId != event.guild?.id)
                return Error("Must be from this guild")

            val guild = guildId?.let { kord.getGuild(it) } ?: return Error("Invalid guild")

            val channel = guild.channels.firstOrNull { it.id == channelId } as? TextChannel
                ?: return Error("Invalid channel")

            messageId?.let { channel.getMessageOrNull(it) } ?: return Error("Invalid message")
        } else {
            arg.toSnowflake()?.let { event.channel.getMessageOrNull(it) } ?: return Error("Invalid ID")
        }

        return Success(message)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.message.id.value)
}