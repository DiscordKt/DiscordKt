package me.jakejmattson.discordkt.internal.listeners

import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.*
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.*
import me.jakejmattson.discordkt.conversations.Conversations
import me.jakejmattson.discordkt.util.trimToID
import me.jakejmattson.discordkt.internal.command.stripMentionInvocation
import me.jakejmattson.discordkt.internal.command.stripPrefixInvocation
import me.jakejmattson.discordkt.internal.utils.Recommender

internal suspend fun registerCommandListener(discord: Discord) = discord.kord.on<MessageCreateEvent> {
    val config = discord.configuration
    val self = kord.selfId.value
    val author = message.author ?: return@on
    val guild = getGuildOrNull()
    val context = DiscordContext(discord, message, author, message.channel, guild)
    val prefix = config.prefix.invoke(context)
    val channel = message.channel.asChannel()
    val content = message.content

    fun String.isBotMention() = config.mentionAsPrefix && (startsWith("<@!$self>") || startsWith("<@$self>"))
    fun String.isSearch() = config.searchCommands && lowercase().startsWith("search") && split(" ").size == 2
    suspend fun search() {
        val query = content.split(" ")[1]

        if (discord.commands.findByName(query) != null)
            message.addReaction(Emojis.whiteCheckMark)
        else if (discord.commands.any { command -> command.names.any { it.contains(query, ignoreCase = true) } })
            message.addReaction(Emojis.ballotBoxWithCheck)
    }

    val rawInputs = when {
        content.startsWith(prefix) -> stripPrefixInvocation(content, prefix)
        content.trimToID() == self.toString() -> {
            config.mentionEmbed.second?.let {
                channel.createEmbed {
                    it.invoke(this, context)
                }
            }

            return@on
        }

        content.isBotMention() -> stripMentionInvocation(content)
        content.isSearch() -> return@on search()
        else -> return@on Conversations.handleMessage(message)
    }

    val (_, commandName, _) = rawInputs

    if (commandName.isBlank()) return@on

    val potentialCommand = discord.commands.findByName(commandName)
    val event = potentialCommand?.buildRequiredEvent(discord, rawInputs, message, author, channel, guild)

    if (event == null) {
        val abortEvent = CommandEvent<TypeContainer>(rawInputs, discord, message, author, channel, guild)
        Recommender.sendRecommendation(abortEvent, commandName)
        return@on
    }

    if (!arePreconditionsPassing(event)) return@on

    val isValidInvocationType = potentialCommand !is SlashCommand || discord.configuration.dualRegistry

    val command = potentialCommand.takeIf { isValidInvocationType && it.hasPermissionToRun(discord, author, guild) }
        ?: return@on Recommender.sendRecommendation(event, commandName)

    if (!config.deleteInvocation)
        config.commandReaction?.let {
            message.addReaction(it)
        }

    command.invoke(event, rawInputs.commandArgs)
}

private fun Command.buildRequiredEvent(discord: Discord, rawInputs: RawInputs, message: Message?, author: User, channel: Channel, guild: Guild?): CommandEvent<TypeContainer>? {
    return try {
        when (this) {
            is GlobalTextCommand -> CommandEvent(rawInputs, discord, message, author, channel as MessageChannel, guild)
            is GuildTextCommand -> GuildCommandEvent(rawInputs, discord, message!!, author, channel as GuildMessageChannel, guild!!)
            is DmTextCommand -> DmCommandEvent(rawInputs, discord, message!!, author, channel as DmChannel)
            is GuildSlashCommand -> GuildSlashCommandEvent(rawInputs, discord, message, author, channel as MessageChannel, guild!!, null)
            is SlashCommand -> SlashCommandEvent(rawInputs, discord, message, author, channel as MessageChannel, guild, null)
        }
    } catch (e: Exception) {
        when (e) {
            is KotlinNullPointerException, is ClassCastException -> null
            else -> throw e
        }
    }
}

internal suspend fun arePreconditionsPassing(event: CommandEvent<*>): Boolean {
    event.discord.preconditions
        .sortedBy { it.priority }
        .forEach { precondition ->
            try {
                precondition.check(event)
            } catch (e: Exception) {
                e.message.takeUnless { it.isNullOrEmpty() }?.let { event.respond(it) }
                return false
            }
        }

    return true
}