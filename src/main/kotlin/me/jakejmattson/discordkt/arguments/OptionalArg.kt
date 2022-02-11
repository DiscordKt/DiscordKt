package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

public data class OptionalData(public val discord: Discord,
                               public val message: Message?,
                               public val author: User,
                               public val channel: MessageChannel,
                               public val guild: Guild?)

internal fun CommandEvent<*>.toOptionalData(): OptionalData = with(this) {
    OptionalData(discord, message, author, channel, guild)
}

/**
 * An optional argument with a default value.
 */
public class OptionalArg<G>(override val name: String,
                            internal val type: Argument<*>,
                            internal val default: suspend OptionalData.() -> G) : Argument<G> {
    override val description: String = internalLocale.optionalArgDescription.inject(type.name)

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<G> {
        val conversion = type.convert(arg, args, event)

        return if (conversion is Success)
            conversion as ArgumentResult<G>
        else
            Success(default.invoke(event.toOptionalData()), 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = type.generateExamples(event)
}