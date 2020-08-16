package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.firstOrNull
import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToSnowflake

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
        val kord = event.discord.kord

        val message = if (isLink) {
            val (guildId, channelId, messageId) = arg.split("/").takeLast(3).map { it.trimToSnowflake() }

            if (!allowsGlobal && guildId != event.guild?.id)
                return Error("Must be from this guild")

            val guild = kord.getGuild(guildId) ?: return Error("Invalid guild")

            val channel = guild.channels.firstOrNull { it.id == channelId } as? TextChannel
                ?: return Error("Invalid channel")

            channel.getMessageOrNull(messageId) ?: return Error("Invalid message")
        } else {
            event.channel.getMessageOrNull(arg.trimToSnowflake()) ?: return Error("Invalid ID")
        }

        return Success(message)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.message.id.toString())
}