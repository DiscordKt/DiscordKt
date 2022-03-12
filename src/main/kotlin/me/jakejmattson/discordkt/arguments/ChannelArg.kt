package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.TextChannel
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a Discord TextChannel entity as an ID or mention.
 *
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
public open class ChannelArg<T : GuildChannel>(override val name: String = "Channel",
                                               override val description: String = internalLocale.channelArgDescription,
                                               private val allowsGlobal: Boolean = false) : ChannelArgument<T> {
    /**
     * Accepts a Discord TextChannel entity as an ID or mention from within this guild.
     */
    public companion object : ChannelArg<TextChannel>()

    override suspend fun transform(input: Channel, context: DiscordContext): Result<T> {
        val channel = input as? T
            ?: return Error("Incorrect channel type")

        if (!allowsGlobal && (channel as GuildChannel).guild.id != context.guild?.id)
            return Error("Must be from this guild")

        return Success(channel)

    }
}