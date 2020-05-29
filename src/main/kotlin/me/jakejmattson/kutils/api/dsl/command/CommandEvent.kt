package me.jakejmattson.kutils.api.dsl.command

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.internal.command.RawInputs
import me.jakejmattson.kutils.internal.utils.Responder
import net.dv8tion.jda.api.entities.*

data class DiscordContext(val discord: Discord,
                          val message: Message,
                          val author: User = message.author,
                          val guild: Guild? = if (message.isFromGuild) message.guild else null,
                          override val channel: MessageChannel = message.channel) : Responder {
    val relevantPrefix: String = discord.configuration.prefix.invoke(this)
}

data class CommandEvent<T : GenericContainer>(val rawInputs: RawInputs,
                                              val container: CommandsContainer,
                                              private val discordContext: DiscordContext) : Responder {
    val discord = discordContext.discord
    val author = discordContext.author
    val message = discordContext.message
    override val channel = discordContext.channel
    val guild = discordContext.guild
    val command = container[rawInputs.commandName]
    val relevantPrefix = discordContext.relevantPrefix

    lateinit var args: T
}

