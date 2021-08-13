package me.jakejmattson.discordkt.api.arguments

import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord TextChannel entity as an ID or mention.
 *
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
open class ChannelArg<T : GuildChannel>(override val name: String = "Channel",
                                        override val description: String = internalLocale.channelArgDescription,
                                        private val allowsGlobal: Boolean = false) : Argument<T> {
    /**
     * Accepts a Discord TextChannel entity as an ID or mention from within this guild.
     */
    companion object : ChannelArg<TextChannel>()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T> {
        val channel = arg.toSnowflakeOrNull()?.let { event.discord.kord.getChannel(it) } as? T
            ?: return Error(internalLocale.notFound)

        if (!allowsGlobal && channel.guild.id != event.guild?.id)
            return Error("Must be from this guild")

        return Success(channel)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.mention)
    override fun formatData(data: T): String = "#${data.name}"
}