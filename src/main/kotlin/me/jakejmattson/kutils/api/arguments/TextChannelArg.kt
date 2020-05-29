package me.jakejmattson.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent
import me.aberrantfox.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.TextChannel

open class TextChannelArg(override val name: String = "TextChannel", private val allowsGlobal: Boolean = false) : ArgumentType<TextChannel>() {
    companion object : TextChannelArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<TextChannel> {
        val channel = tryRetrieveSnowflake(event.discord.jda) {
            it.getTextChannelById(arg.trimToID())
        } as TextChannel? ?: return ArgumentResult.Error("Couldn't retrieve text channel: $arg")

        if (!allowsGlobal && channel.guild.id != event.guild?.id)
            return ArgumentResult.Error("Text channel must be from this guild.")

        return ArgumentResult.Success(channel)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.id)
}