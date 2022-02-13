package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.GuildInteractionBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.bundleToContainer
import me.jakejmattson.discordkt.commands.*
import me.jakejmattson.discordkt.conversations.Conversations
import me.jakejmattson.discordkt.dsl.Menu
import me.jakejmattson.discordkt.internal.utils.InternalLogger

@OptIn(KordUnsafe::class)
@KordPreview
internal suspend fun registerInteractionListener(discord: Discord) = discord.kord.on<InteractionCreateEvent> {
    when (val interaction = interaction) {
        is ChatInputCommandInteraction -> handleSlashCommand(interaction, discord)
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
        listOf(interaction.users.values.first())
    }
}

private suspend fun handleMessageContext(interaction: MessageCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction, discord) {
        listOf(interaction.messages.values.first())
    }
}

private suspend fun handleSlashCommand(interaction: ChatInputCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction as ApplicationCommandInteraction, discord) { optionalData ->
        simplifySlashArgs(execution.arguments.map { it to interaction.command.options[it.name.lowercase()] }, optionalData)
    }
}

private suspend fun handleApplicationCommand(interaction: ApplicationCommandInteraction, discord: Discord, input: suspend SlashCommand.(OptionalData) -> List<Any?>) {
    val dktCommand = discord.commands.filterIsInstance<GuildSlashCommand>().firstOrNull { it.appName == interaction.name || it.name.equals(interaction.name, true) }
        ?: discord.commands.filterIsInstance<GlobalSlashCommand>().firstOrNull { it.appName == interaction.name || it.name.equals(interaction.name, true) }
        ?: return

    val author = interaction.user.asUser()
    val guild = (interaction as? GuildInteractionBehavior)?.getGuild()
    val channel = interaction.getChannel()
    val message = interaction.data.message.value?.let { Message(it, discord.kord) }

    val optionalData = OptionalData(discord, message, author, channel, guild)
    val data = input.invoke(dktCommand, optionalData)

    val rawInputs = RawInputs("/${dktCommand.name} ${data.map { it.toString() }}", dktCommand.name, prefixCount = 1)

    val event =
        if (dktCommand is GuildSlashCommand)
            GuildSlashCommandEvent(rawInputs, discord, message, author, channel, guild!!, interaction)
        else
            SlashCommandEvent<TypeContainer>(rawInputs, discord, message, author, channel, guild, interaction)

    if (!arePreconditionsPassing(event)) return

    event.args = bundleToContainer(data)

    (dktCommand.execution as Execution<CommandEvent<*>>).execute(event)
}

@OptIn(KordPreview::class)
private fun simplifySlashArgs(complexArgs: List<Pair<Argument<*>, OptionValue<*>?>>, optionalData: OptionalData) =
    complexArgs.map { (arg, value) ->
        if (value == null) {
            require(arg is OptionalArg<*>) { "${arg.name} could not be mapped by name." }
            runBlocking { arg.default.invoke(optionalData) }
        } else
            when (if (arg is OptionalArg) arg.type else arg) {
                is MultipleArg<*> -> listOf(value.value)
                is ChannelArg<*> -> value.channel()
                else -> value.value
            }
    }