package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.jda.tryRetrieveSnowflake
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.*

open class MessageArg(override val name: String = "Message", private val allowsGlobal: Boolean = false) : ArgumentType<Message>() {
    companion object : MessageArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Message> {
        val regex = "https://discordapp.com/channels/\\d+/\\d+/\\d+".toRegex()
        val isLink = regex.matches(arg)
        val jda = event.discord.jda

        fun <T> generateError(clarification: String)
            = ArgumentResult.Error<T>("Couldn't retrieve $name from $arg ($clarification).")

        val message = if (isLink) {
            val (guildId, channelId, messageId) = arg.split("/").takeLast(3)

            if (!allowsGlobal && guildId != event.guild?.id)
                return generateError("Must be from this guild")

            val guild = jda.guilds.firstOrNull { it.id == guildId }
                ?: return generateError("Invalid guild")

            val channel = guild.channels.filterIsInstance<TextChannel>().firstOrNull { it.id == channelId }
                ?: return generateError("Invalid channel")

            channel.retrieveMessageById(messageId).complete()
                ?: return generateError("Invalid message")
        } else {
            jda.tryRetrieveSnowflake {
                event.channel.retrieveMessageById(arg.trimToID()).complete()
            } as Message? ?: return generateError("Invalid ID")
        }

        return ArgumentResult.Success(message)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.message.id)
}