package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.*

open class MessageArg(override val name: String = "Message", private val allowsGlobal: Boolean = false) : ArgumentType<Message>() {
    companion object : MessageArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Message> {
        val regex = "https://discordapp.com/channels/\\d+/\\d+/\\d+".toRegex()
        val isLink = regex.matches(arg)

        val message = if (isLink) {
            val (guildId, channelId, messageId) = arg.split("/").takeLast(3)

            if (!allowsGlobal && guildId != event.guild?.id)
                return ArgumentResult.Error("Message links must be from this guild.")

            val guild = event.discord.jda.guilds.firstOrNull { it.id == guildId }
                ?: return ArgumentResult.Error("No mutual guilds with the message link provided.")

            val channel = guild.channels.filterIsInstance<TextChannel>().firstOrNull { it.id == channelId }
                ?: return ArgumentResult.Error("Could not find the channel from the message link provided.")

            channel.retrieveMessageById(messageId).complete()
                ?: return ArgumentResult.Error("Could not find the message from the message link provided.")
        } else {
            tryRetrieveSnowflake(event.discord.jda) {
                event.channel.retrieveMessageById(arg.trimToID()).complete()
            } as Message? ?: return ArgumentResult.Error("Couldn't retrieve message by ID.")
        }

        return ArgumentResult.Success(message)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.message.id)
}