package me.aberrantfox.kjdautils.api.dsl.command

import kotlinx.coroutines.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.command.CommandStruct
import net.dv8tion.jda.api.entities.*

data class DiscordContext(
    val stealthInvocation: Boolean,
    val discord: Discord,
    val message: Message,
    val author: User = message.author,
    val channel: MessageChannel = message.channel,
    val guild: Guild? = null)
{
    fun respond(msg: String) = unsafeRespond(msg.sanitiseMentions())
    fun respond(embed: MessageEmbed) = this.channel.sendMessage(embed).queue()

    fun respondTimed(msg: String, millis: Long = 5000) {
        require(millis >= 0) { "RespondTimed: Delay cannot be negative." }

        this.channel.sendMessage(msg.sanitiseMentions()).queue {
            GlobalScope.launch {
                delay(millis)
                it.delete().queue()
            }
        }
    }

    fun respondTimed(embed: MessageEmbed, millis: Long = 5000) {
        require(millis >= 0) { "RespondTimed: Delay cannot be negative." }

        this.channel.sendMessage(embed).queue {
            GlobalScope.launch {
                delay(millis)
                it.delete().queue()
            }
        }
    }

    fun unsafeRespond(msg: String) =
        if(msg.length > 2000){
            val toSend = msg.chunked(2000)
            toSend.forEach{ channel.sendMessage(it).queue() }
        } else{
            channel.sendMessage(msg).queue()
        }
}

data class CommandEvent<T: ArgumentContainer>(
    val commandStruct: CommandStruct,
    val container: CommandsContainer,
    private val discordContext: DiscordContext
) {
    val stealthInvocation = discordContext.stealthInvocation
    val discord = discordContext.discord
    val author = discordContext.author
    val message = discordContext.message
    val channel = discordContext.channel
    val guild = discordContext.guild
    val command = container[commandStruct.commandName]

    var args: T = NoArg() as T

    fun respond(msg: String) = discordContext.respond(msg)
    fun respond(embed: MessageEmbed) = discordContext.respond(embed)
    fun respond(construct: EmbedDSLHandle.() -> Unit) = respond(embed (construct))
    fun respondTimed(msg: String, millis: Long = 5000) = discordContext.respondTimed(msg, millis)
    fun respondTimed(embed: MessageEmbed, millis: Long = 5000) = discordContext.respondTimed(embed, millis)
    fun unsafeRespond(msg: String) = discordContext.unsafeRespond(msg)
}