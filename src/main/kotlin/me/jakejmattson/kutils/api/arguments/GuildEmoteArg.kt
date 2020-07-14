package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.Emote

/**
 * Accepts a guild emote.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class GuildEmoteArg(override val name: String = "Guild Emote", private val allowsGlobal: Boolean = false) : ArgumentType<Emote>() {
    /**
     * Accepts a guild emote from within this guild.
     */
    companion object : GuildEmoteArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Emote> {
        val trimmed = arg.trimToID()
        val split = trimmed.split(":")

        val id = when (split.size) {
            1 -> split[0]
            3 -> split[2]
            else -> return Error("Couldn't retrieve $name from $arg.")
        }

        val availableEmotes =
            if (allowsGlobal)
                event.discord.jda.guilds.flatMap { it.emotes }
            else
                event.guild?.emotes

        availableEmotes ?: return Error("Could not find a guild to fetch emojis from.")

        val emote = availableEmotes.firstOrNull { it.id == id }
            ?: return Error("Could not find an emoji${if (!allowsGlobal) " in this guild " else " "}with the ID: $id")

        return Success(emote)
    }

    override fun generateExamples(event: CommandEvent<*>) = event.guild?.emotes?.map { it.asMention } ?: emptyList()
}