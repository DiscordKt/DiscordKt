package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.*

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

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Message> {
        val regex = "https://discordapp.com/channels/\\d+/\\d+/\\d+".toRegex()
        val isLink = regex.matches(arg)
        val jda = event.discord.jda

        val message = if (isLink) {
            val (guildId, channelId, messageId) = arg.split("/").takeLast(3)

            if (!allowsGlobal && guildId != event.guild?.id)
                return Error("Must be from this guild")

            val guild = jda.guilds.firstOrNull { it.id == guildId }
                ?: return Error("Invalid guild")

            val channel = guild.channels.filterIsInstance<TextChannel>().firstOrNull { it.id == channelId }
                ?: return Error("Invalid channel")

            channel.retrieveMessageById(messageId).complete()
                ?: return Error("Invalid message")
        } else {
            event.discord.retrieveSnowflake {
                event.channel.retrieveMessageById(arg.trimToID()).complete()
            } as Message? ?: return Error("Invalid ID")
        }

        return Success(message)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.message.id)
}