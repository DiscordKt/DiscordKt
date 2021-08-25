package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.GuildInteractionBehavior
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.commands.*
import me.jakejmattson.discordkt.api.conversations.Conversations
import me.jakejmattson.discordkt.api.dsl.Menu

@OptIn(KordUnsafe::class)
@KordPreview
internal suspend fun registerInteractionListener(discord: Discord) = discord.kord.on<InteractionCreateEvent> {
    val interaction = interaction

    if (interaction is ComponentInteraction) {
        Menu.handleButtonPress(interaction)
        Conversations.handleInteraction(interaction)
        return@on
    }

    if (interaction !is ChatInputCommandInteraction)
        return@on

    val dktCommand = discord.commandOfType<GuildSlashCommand>(interaction.command.rootName)
        ?: discord.commandOfType<GlobalSlashCommand>(interaction.command.rootName) ?: return@on

    interaction.acknowledgePublic().followUp {
        content = Emojis.whiteCheckMark.unicode
    }

    val complexArgs = dktCommand.executions.first().arguments.map { it to interaction.command.options[it.name.lowercase()]!! }
    val args = simplifySlashArgs(complexArgs)
    val rawInputs = RawInputs("/${dktCommand.name} $args", dktCommand.name, prefixCount = 1)
    val author = interaction.user.asUser()
    val guild = (interaction as? GuildInteractionBehavior)?.getGuild()
    val channel = interaction.getChannel()
    val event = SlashCommandEvent<TypeContainer>(rawInputs, discord, interaction.data.message.value?.let { Message(it, kord) }, author, channel, guild)

    if (!arePreconditionsPassing(event)) return@on

    dktCommand.invoke(event, rawInputs.commandArgs)
}

@OptIn(KordPreview::class)
private fun simplifySlashArgs(complexArgs: List<Pair<Argument<*>, OptionValue<*>>>) =
    complexArgs.joinToString(" ") { (arg, optionalValue) ->
        when (arg) {
            is IntegerArg -> optionalValue.int().toString()
            is BooleanArg -> optionalValue.boolean().toString()
            is UserArg -> optionalValue.user().id.asString
            is RoleArg -> optionalValue.role().id.asString
            is ChannelArg<*> -> optionalValue.channel().id.asString
            else -> optionalValue.string()
        }
    }