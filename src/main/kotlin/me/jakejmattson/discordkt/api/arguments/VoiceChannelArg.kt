package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.channel.VoiceChannel
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflake

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

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<VoiceChannel> {
        val channel = arg.toSnowflake()?.let { event.discord.api.getChannel(it) } as? VoiceChannel
            ?: return Error("Not found")

        if (!allowsGlobal && channel.guild.id != event.guild?.id)
            return Error("Must be from this guild")

        return Success(channel)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("Voice Channel ID")
    override fun formatData(data: VoiceChannel) = data.name
}