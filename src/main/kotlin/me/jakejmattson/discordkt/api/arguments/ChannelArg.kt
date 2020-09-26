package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.channel.*
import me.jakejmattson.discordkt.api.dsl.GlobalCommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord TextChannel entity as an ID or mention.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class ChannelArg<T : GuildChannel>(override val name: String = "Text Channel", private val allowsGlobal: Boolean = false) : ArgumentType<T>() {
    /**
     * Accepts a Discord TextChannel entity as an ID or mention from within this guild.
     */
    companion object : ChannelArg<TextChannel>()

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>): ArgumentResult<T> {
        val channel = arg.toSnowflakeOrNull()?.let { event.discord.api.getChannel(it) } as? T
            ?: return Error("Not found")

        if (!allowsGlobal && channel.guild.id != event.guild?.id)
            return Error("Must be from this guild")

        return Success(channel)
    }

    override fun generateExamples(event: GlobalCommandEvent<*>) = listOf(event.channel.id.value)

    override fun formatData(data: T): String = "#${data.name}"
}