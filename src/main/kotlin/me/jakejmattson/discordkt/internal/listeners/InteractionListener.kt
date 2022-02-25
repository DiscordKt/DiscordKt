package me.jakejmattson.discordkt.internal.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.GuildInteractionBehavior
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
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
        listOf(Success(interaction.users.values.first()))
    }
}

private suspend fun handleMessageContext(interaction: MessageCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction, discord) {
        listOf(Success(interaction.messages.values.first()))
    }
}

private suspend fun handleSlashCommand(interaction: ChatInputCommandInteraction, discord: Discord) {
    handleApplicationCommand(interaction as ApplicationCommandInteraction, discord) { optionalData ->
        transformInput(execution.arguments.map {
            it to if (it is AttachmentArgument) interaction.command.attachments[it.name.lowercase()] else interaction.command.options[it.name.lowercase()]?.value
        }, optionalData)
    }
}

private suspend fun handleApplicationCommand(interaction: ApplicationCommandInteraction, discord: Discord, input: suspend SlashCommand.(DiscordContext) -> List<ArgumentResult<*>>) {
    val dktCommand = discord.commands.filterIsInstance<GuildSlashCommand>().firstOrNull { it.appName == interaction.invokedCommandName || it.name.equals(interaction.invokedCommandName, true) }
        ?: discord.commands.filterIsInstance<GlobalSlashCommand>().firstOrNull { it.appName == interaction.invokedCommandName || it.name.equals(interaction.invokedCommandName, true) }
        ?: return

    val author = interaction.user.asUser()
    val guild = (interaction as? GuildInteractionBehavior)?.getGuild()
    val channel = interaction.getChannel()
    val message = interaction.data.message.value?.let { Message(it, discord.kord) }

    val context = DiscordContext(discord, message, author, channel, guild)
    val transformResults = input.invoke(dktCommand, context)
    val rawInputs = RawInputs("/${dktCommand.name} ${transformResults.map { it.toString() }}", dktCommand.name, prefixCount = 1)

    val event =
        if (dktCommand is GuildSlashCommand)
            GuildSlashCommandEvent(rawInputs, discord, message, author, channel, guild!!, interaction)
        else
            SlashCommandEvent<TypeContainer>(rawInputs, discord, message, author, channel, guild, interaction)

    if (!arePreconditionsPassing(event)) return

    val error = transformResults.find { it is Error<*> } as Error<*>?

    if (error != null) {
        event.respond(error.error)
        return
    }

    val castData = transformResults.map {
        (it as Success).result
    }

    event.args = bundleToContainer(castData)

    (dktCommand.execution as Execution<CommandEvent<*>>).execute(event)
}

@OptIn(KordPreview::class)
private suspend fun transformInput(complexArgs: List<Pair<Argument<*, *>, Any?>>, context: DiscordContext) =
    complexArgs.map { (rawArg, value) ->
        if (value == null) {
            require(rawArg is OptionalArg<*, *>) { "${rawArg.name} could not be mapped by name." }
            runBlocking { Success(rawArg.default.invoke(context)) }
        } else {
            val arg = when (rawArg) {
                is OptionalArg -> rawArg.type
                is MultipleArg<*, *> -> rawArg.base
                else -> rawArg
            }

            val parsedValue = when (arg) {
                is AttachmentArgument -> value
                is EntityArgument -> arg.parse(mutableListOf(value.toString()), context.discord)
                else -> value
            }

            when (arg) {
                //Simple
                is StringArgument -> arg.transform(parsedValue as String, context)
                is IntegerArgument -> arg.transform(parsedValue as Int, context)
                is DoubleArgument -> arg.transform(parsedValue as Double, context)
                is BooleanArgument -> arg.transform(parsedValue as Boolean, context)

                //Entity
                is UserArgument -> arg.transform(parsedValue as User, context)
                is RoleArgument -> arg.transform(parsedValue as Role, context)
                is ChannelArgument -> arg.transform(parsedValue as Channel, context)
                is AttachmentArgument -> arg.transform(parsedValue as Attachment, context)

                //Unknown
                else -> Success(value)
            }
        }
    }