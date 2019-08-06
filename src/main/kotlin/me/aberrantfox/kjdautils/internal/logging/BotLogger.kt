package me.aberrantfox.kjdautils.internal.logging


import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel


interface BotLogger {
    fun info(message: String)
    fun info(message: MessageEmbed)
    fun cmd(message: String)
    fun cmd(message: MessageEmbed)
    fun error(message: String)
    fun error(message: MessageEmbed)
    fun alert(message: String)
    fun alert(message: MessageEmbed)
    fun voice(message: String)
    fun voice(message: MessageEmbed)
    fun history(message: String)
    fun history(message: MessageEmbed)
}

data class ChannelIdHolder(val info: String = "insert-id-here",
                           val cmd: String = "insert-id-here",
                           val error: String = "insert-id-here",
                           val alert: String = "insert-id-here",
                           val voice: String = "insert-id-here",
                           val history: String = "insert-id-here")

fun convertChannels(holder: ChannelIdHolder, jda: JDA): BotLogger =
    ChannelLogger(Channels(
        jda.getTextChannelById(holder.info)!!,
        jda.getTextChannelById(holder.cmd)!!,
        jda.getTextChannelById(holder.error)!!,
        jda.getTextChannelById(holder.alert)!!,
        jda.getTextChannelById(holder.voice)!!,
        jda.getTextChannelById(holder.history)!!))

data class Channels(val info: TextChannel,
                    val cmd: TextChannel,
                    val error: TextChannel,
                    val alert: TextChannel,
                    val voice: TextChannel,
                    val history: TextChannel)

class DefaultLogger : BotLogger {
    override fun info(message: String) {}
    override fun info(message: MessageEmbed) {}
    override fun cmd(message: String) {}
    override fun cmd(message: MessageEmbed) {}

    override fun error(message: String) {}
    override fun error(message: MessageEmbed) {}

    override fun alert(message: String) {}
    override fun alert(message: MessageEmbed) {}

    override fun voice(message: String) {}
    override fun voice(message: MessageEmbed) {}

    override fun history(message: String) {}
    override fun history(message: MessageEmbed) {}
}

class ChannelLogger(private val channels: Channels) : BotLogger {
    override fun info(message: String) = channels.info.sendMessage(message).queue()
    override fun info(message: MessageEmbed) = channels.info.sendMessage(message).queue()

    override fun cmd(message: String) = channels.cmd.sendMessage(message).queue()
    override fun cmd(message: MessageEmbed) = channels.cmd.sendMessage(message).queue()

    override fun error(message: String) = channels.error.sendMessage(message).queue()
    override fun error(message: MessageEmbed) = channels.error.sendMessage(message).queue()

    override fun alert(message: String) = channels.alert.sendMessage(message).queue()
    override fun alert(message: MessageEmbed) = channels.alert.sendMessage(message).queue()

    override fun voice(message: String) = channels.voice.sendMessage(message).queue()
    override fun voice(message: MessageEmbed) = channels.voice.sendMessage(message).queue()

    override fun history(message: String) = channels.history.sendMessage(message).queue()
    override fun history(message: MessageEmbed) = channels.history.sendMessage(message).queue()
}

