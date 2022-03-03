package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.GuildInteractionBehavior
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
import me.jakejmattson.discordkt.internal.command.transformArgs
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
        Success(bundleToContainer(listOf(interaction.users.values.first())))
    }
}

private suspend fun handleMessageContext(interaction: MessageCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction, discord) {
        Success(bundleToContainer(listOf(interaction.messages.values.first())))
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

                    //Entity
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
    val dktCommand = discord.commands.filterIsInstance<GuildSlashCommand>().firstOrNull { it.appName == interaction.invokedCommandName || it.name.equals(interaction.invokedCommandName, true) }
        ?: discord.commands.filterIsInstance<GlobalSlashCommand>().firstOrNull { it.appName == interaction.invokedCommandName || it.name.equals(interaction.invokedCommandName, true) }
        ?: return

    val author = interaction.user.asUser()
    val guild = (interaction as? GuildInteractionBehavior)?.getGuild()
    val channel = interaction.getChannel()
    val message = interaction.data.message.value?.let { Message(it, discord.kord) }

    val context = DiscordContext(discord, message, author, channel, guild)
    val transformResults = input.invoke(dktCommand, context)
    val rawInputs = RawInputs("/${dktCommand.name}", dktCommand.name, prefixCount = 1)

    val event =
        if (dktCommand is GuildSlashCommand)
            GuildSlashCommandEvent(rawInputs, discord, message, author, channel, guild!!, interaction)
        else
            SlashCommandEvent<TypeContainer>(rawInputs, discord, message, author, channel, guild, interaction)

    if (!arePreconditionsPassing(event)) return

    event.args = when (transformResults) {
        is Error -> {
            event.respond(transformResults.error)
            return
        }
        is Success<*> -> transformResults.result as TypeContainer
    }

    (dktCommand.execution as Execution<CommandEvent<*>>).execute(event)
}