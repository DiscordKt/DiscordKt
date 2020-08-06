package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.*

/**
 * Accepts a Discord VoiceChannel entity as an ID or mention.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class VoiceChannelArg(override val name: String = "Voice Channel", private val allowsGlobal: Boolean = false) : ArgumentType<VoiceChannel>() {
    /**
     * Accepts a Discord VoiceChannel entity as an ID or mention from within this guild.
     */
    companion object : VoiceChannelArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<VoiceChannel> {
        val channel = event.discord.retrieveSnowflake {
            it.getVoiceChannelById(arg.trimToID())
        } as VoiceChannel? ?: return Error("Not found")

        if (!allowsGlobal && channel.guild.id != event.guild?.id)
            return Error("Must be from this guild")

        return Success(channel)
    }

    override fun generateExamples(event: CommandEvent<*>): List<String> {
        val channel = event.guild?.channels?.firstOrNull { it.type == ChannelType.VOICE } as? VoiceChannel
        return listOf(channel?.id ?: "582168201979494421")
    }

    override fun formatData(data: VoiceChannel) = data.name
}