package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.GuildInteractionBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.*
import me.jakejmattson.discordkt.conversations.Conversations
import me.jakejmattson.discordkt.dsl.Menu
import me.jakejmattson.discordkt.internal.utils.InternalLogger

@OptIn(KordUnsafe::class)
@KordPreview
internal suspend fun registerInteractionListener(discord: Discord) = discord.kord.on<InteractionCreateEvent> {
    when (val interaction = interaction) {
        is ChatInputCommandInvocationInteraction -> handleSlashCommand(interaction, discord)
        is MessageCommandInteraction -> handleMessageContext(interaction, discord)
        is UserCommandInteraction -> handleUserContext(interaction, discord)
        is SelectMenuInteraction -> Conversations.handleInteraction(interaction)
        is ButtonInteraction -> {
            Menu.handleButtonPress(interaction)
            Conversations.handleInteraction(interaction)
        }
        else -> InternalLogger.error("Unknown interaction received.")
    }
}

private suspend fun handleUserContext(interaction: UserCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction, discord) {
        interaction.users.values.first().id.toString()
    }
}

private suspend fun handleMessageContext(interaction: MessageCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction, discord) {
        interaction.messages.values.first().id.toString()
    }
}

private suspend fun handleSlashCommand(interaction: ChatInputCommandInvocationInteraction, discord: Discord) {
    handleApplicationCommand(interaction, discord) {
        simplifySlashArgs(execution.arguments.map { it to interaction.command.options[it.name.lowercase()]!! })
    }
}

private suspend fun handleApplicationCommand(interaction: ApplicationCommandInteraction, discord: Discord, args: suspend SlashCommand.() -> String) {
    val dktCommand = discord.commands.filterIsInstance<GuildSlashCommand>().firstOrNull { it.appName == interaction.name || it.name.equals(interaction.name, true) }
        ?: discord.commands.filterIsInstance<GlobalSlashCommand>().firstOrNull { it.appName == interaction.name || it.name.equals(interaction.name, true) }
        ?: return

    val rawInputs = RawInputs("/${dktCommand.name} ${args.invoke(dktCommand)}", dktCommand.name, prefixCount = 1)
    val author = interaction.user.asUser()
    val guild = (interaction as? GuildInteractionBehavior)?.getGuild()
    val channel = interaction.getChannel()
    val event =
        if (dktCommand is GuildSlashCommand)
            GuildSlashCommandEvent(rawInputs, discord, interaction.data.message.value?.let { Message(it, discord.kord) }, author, channel, guild!!, interaction)
        else
            SlashCommandEvent<TypeContainer>(rawInputs, discord, interaction.data.message.value?.let { Message(it, discord.kord) }, author, channel, guild, interaction)

    if (!arePreconditionsPassing(event)) return

    dktCommand.invoke(event, rawInputs.commandArgs)
}

@OptIn(KordPreview::class)
private fun simplifySlashArgs(complexArgs: List<Pair<Argument<*>, OptionValue<*>>>) =
    complexArgs.joinToString(" ") { (arg, optionalValue) ->
        when (if (arg is OptionalArg) arg.type else arg) {
            is IntegerArg -> optionalValue.int().toString()
            is DoubleArg -> optionalValue.number().toString()
            is BooleanArg -> optionalValue.boolean().toString()
            is UserArg -> optionalValue.user().id.toString()
            is MemberArg -> optionalValue.member().id.toString()
            is RoleArg -> optionalValue.role().id.toString()
            is ChannelArg<*> -> optionalValue.channel().id.toString()
            else -> optionalValue.string()
        }
    }