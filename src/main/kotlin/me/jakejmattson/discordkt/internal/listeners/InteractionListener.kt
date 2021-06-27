package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.dsl.*

@KordPreview
internal suspend fun registerInteractionListener(discord: Discord) = discord.kord.on<InteractionCreateEvent> {
    val interaction = interaction

    if (interaction is ComponentInteraction) {
        Menu.handleButtonPress(interaction)
        return@on
    }

    if (interaction !is CommandInteraction)
        return@on

    val dktCommand = discord.commands[interaction.command.rootName] as? GlobalSlashCommand ?: return@on
    val args = dktCommand.executions.first().arguments.joinToString(" ") { interaction.command.options[it.name.lowercase()]!!.value.toString() }
    val rawInputs = RawInputs("/${dktCommand.name} $args", dktCommand.name, prefixCount = 1)
    val author = kord.getUser(interaction.data.user.value!!.id)!!
    val guild = (interaction as? GuildInteraction)?.getGuild()
    val channel = interaction.getChannel()
    val event = SlashCommandEvent<TypeContainer>(rawInputs, discord, channel.getLastMessage()!!, author, channel, guild)

    if (!arePreconditionsPassing(event)) return@on

    dktCommand.invoke(event, rawInputs.commandArgs)
}