package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflake

/**
 * Accepts a Discord TextChannel entity as an ID or mention.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class TextChannelArg(override val name: String = "Text Channel", private val allowsGlobal: Boolean = false) : ArgumentType<TextChannel>() {
    /**
     * Accepts a Discord TextChannel entity as an ID or mention from within this guild.
     */
    companion object : TextChannelArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<TextChannel> {
        val channel = arg.toSnowflake()?.let { event.discord.api.getChannel(it) } as? TextChannel
            ?: return Error("Not found")

        if (!allowsGlobal && channel.id != event.guild?.id)
            return Error("Must be from this guild.")

        return Success(channel)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.id.longValue.toString())

    override fun formatData(data: TextChannel) = "#${data.name}"
}