package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.bundleToContainer
import me.jakejmattson.discordkt.commands.*
import me.jakejmattson.discordkt.conversations.Conversations
import me.jakejmattson.discordkt.dsl.Menu
import me.jakejmattson.discordkt.dsl.modalBuffer
import me.jakejmattson.discordkt.internal.command.transformArgs
import me.jakejmattson.discordkt.internal.utils.InternalLogger

@KordPreview
internal suspend fun registerInteractionListener(discord: Discord) = discord.kord.on<InteractionCreateEvent> {
    when (val interaction = interaction) {
        is MessageCommandInteraction -> handleApplicationCommand(interaction, discord) { Success(bundleToContainer(listOf(interaction.messages.values.first()))) }
        is UserCommandInteraction -> handleApplicationCommand(interaction, discord) { Success(bundleToContainer(listOf(interaction.users.values.first()))) }
        is ChatInputCommandInteraction -> handleSlashCommand(interaction, discord)
        is AutoCompleteInteraction -> handleAutocomplete(interaction, discord)
        is ModalSubmitInteraction -> modalBuffer.send(interaction)
        is SelectMenuInteraction -> Conversations.handleInteraction(interaction)
        is ButtonInteraction -> {
            Menu.handleButtonPress(interaction)
            Conversations.handleInteraction(interaction)
        }

        else -> InternalLogger.error("Unknown interaction received: ${interaction.javaClass.simpleName}")
    }
}

private suspend fun handleSlashCommand(interaction: ChatInputCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction as ApplicationCommandInteraction, discord) { context ->
        transformArgs(execution.arguments.map { argument ->
            val argName = argument.name.lowercase()
            val arg = if (argument is WrappedArgument<*, *, *, *>) argument.innerType else argument

            with(interaction.command) {
                val value = when (arg) {
                    is StringArgument -> strings[argName]
                    is IntegerArgument -> integers[argName]?.toInt()
                    is DoubleArgument -> numbers[argName]
                    is BooleanArgument -> booleans[argName]
                    is UserArgument -> users[argName]
                    is RoleArgument -> roles[argName]
                    is ChannelArgument -> channels[argName]?.let { kord.getChannel(it.id) }
                    is AttachmentArgument -> attachments[argName]
                    else -> options[argName]?.value
                }

                if (argument is MultipleArg<*, *>)
                    argument to listOf(value)
                else
                    argument to value
            }
        }, context)
    }
}

private suspend fun handleApplicationCommand(interaction: ApplicationCommandInteraction, discord: Discord, input: suspend SlashCommand.(DiscordContext) -> Result<*>) {
    val dktCommand = findDktCommand(interaction, discord) ?: return
    val author = interaction.user.asUser()
    val guild = (interaction as? GuildInteractionBehavior)?.getGuild()
    val channel = interaction.getChannel()
    val message = interaction.data.message.value?.let { Message(it, discord.kord) }
    val context = DiscordContext(discord, message, author, channel, guild)
    val transformResults = input.invoke(dktCommand, context)
    val rawInputs = RawInputs("/${dktCommand.name}", dktCommand.name, prefixCount = 1)

    val event =
        if (dktCommand is GuildSlashCommand)
            GuildSlashCommandEvent(rawInputs, discord, message, author, channel, guild!!, interaction as GuildApplicationCommandInteraction)
        else
            SlashCommandEvent<TypeContainer>(rawInputs, discord, message, author, channel, guild, interaction)

    if (!arePreconditionsPassing(event)) return

    event.args = when (transformResults) {
        is Success<*> -> transformResults.result as TypeContainer
        is Error -> {
            event.respond(transformResults.error)
            return
        }
    }

    dktCommand.execution.execute(event)
}

private suspend fun handleAutocomplete(interaction: AutoCompleteInteraction, discord: Discord) {
    val dktCommand = findDktCommand(interaction, discord) ?: return
    val argName = interaction.command.options.entries.single { it.value.focused }.key.lowercase()
    val rawArg = dktCommand.execution.arguments.first { it.name.equals(argName, true) }

    val arg: AutocompleteArg<*, *> = if (rawArg is WrappedArgument<*, *, *, *>)
        if (rawArg is AutocompleteArg<*, *>)
            rawArg
        else
            rawArg.type as AutocompleteArg<*, *>
    else
        return

    val currentInput = interaction.focusedOption.value
    val autocompleteData = AutocompleteData(interaction, currentInput)
    val suggestions = arg.autocomplete.invoke(autocompleteData).take(25)

    when (arg.innerType) {
        is IntegerArgument -> interaction.suggestInt { suggestions.forEach { choice(it.toString(), (it as Int).toLong()) } }
        is DoubleArgument -> interaction.suggestNumber { suggestions.forEach { choice(it.toString(), it as Double) } }
        is StringArgument -> interaction.suggestString { suggestions.forEach { choice(it.toString(), it as String) } }
        else -> {}
    }
}

private fun findDktCommand(interaction: Interaction, discord: Discord): SlashCommand? {
    val slashCommands = discord.commands.filterIsInstance<SlashCommand>()
    val contextCommands = discord.commands.filterIsInstance<ContextCommand>()

    fun handleSubcommand(command: InteractionCommand) =
        if (command is SubCommand)
            discord
                .subcommands.find { it.name.equals(command.rootName, true) }!!
                .commands.findByName(command.name)
        else
            slashCommands.findByName(command.rootName)

    return when (interaction) {
        is ChatInputCommandInteraction -> handleSubcommand(interaction.command)
        is AutoCompleteInteraction -> handleSubcommand(interaction.command)
        is MessageCommandInteraction -> contextCommands.find { it.displayText == interaction.invokedCommandName }
        is UserCommandInteraction -> contextCommands.find { it.displayText == interaction.invokedCommandName }
        is ModalSubmitInteraction, is SelectMenuInteraction, is ButtonInteraction -> null
    }
}