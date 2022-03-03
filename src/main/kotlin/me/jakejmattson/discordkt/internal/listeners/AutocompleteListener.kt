package me.jakejmattson.discordkt.internal.listeners

import dev.kord.core.behavior.interaction.suggestInt
import dev.kord.core.behavior.interaction.suggestNumber
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.event.interaction.GuildAutoCompleteInteractionCreateEvent
import dev.kord.core.on
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.GuildSlashCommand

internal suspend fun registerAutocompleteListener(discord: Discord) = discord.kord.on<GuildAutoCompleteInteractionCreateEvent> {
    val dktCommand = discord.commands.filterIsInstance<GuildSlashCommand>().firstOrNull { it.name.equals(interaction.command.rootName, true) }
        ?: return@on

    val argName = interaction.command.options.entries.single { it.value.focused }.key.lowercase()
    val rawArg = dktCommand.execution.arguments.first { it.name.equals(argName, true) }

    val arg: AutocompleteArg<*, *> = if (rawArg is WrappedArgument<*, *, *, *>)
        if (rawArg is AutocompleteArg<*, *>)
            rawArg
        else
            rawArg.type as AutocompleteArg<*, *>
    else
        return@on

    val currentInput = interaction.focusedOption.value
    val autocompleteData = AutocompleteData(interaction, currentInput)
    val suggestions = arg.autocomplete.invoke(autocompleteData).take(25)

    when (arg.innerType) {
        is IntegerArgument -> interaction.suggestInt {
            suggestions.forEach { choice(it.toString(), (it as Int).toLong()) }
        }
        is DoubleArgument -> interaction.suggestNumber {
            suggestions.forEach { choice(it.toString(), it as Double) }
        }
        is StringArgument -> interaction.suggestString {
            suggestions.forEach { choice(it.toString(), it as String) }
        }
        else -> {}
    }
}
