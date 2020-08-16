package me.jakejmattson.discordkt.internal.listeners

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.kordx.emoji.toReaction
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.api.dsl.preconditions.*
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToID
import me.jakejmattson.discordkt.api.services.ConversationService
import me.jakejmattson.discordkt.internal.command.*

suspend fun registerCommandListener(container: CommandsContainer, discord: Discord, preconditions: List<Precondition>) = discord.kord.on<MessageCreateEvent> {
    val config = discord.configuration
    val author = message.author?.takeUnless { it.isBot ?: false } ?: return@on
    val channel = message.channel
    val content = message.content
    val discordContext = DiscordContext(discord, message, guild = message.getGuildOrNull())
    val prefix = config.prefix.invoke(discordContext)
    val conversationService = discord.getInjectionObjects(ConversationService::class)

    fun isPrefixInvocation(message: String, prefix: String) = message.startsWith(prefix)

    suspend fun isMentionInvocation(message: String): Boolean {
        if (!config.allowMentionPrefix)
            return false

        val id = discord.kord.getSelf().id

        return message.startsWith("<@!$id>") || message.startsWith("<@$id>")
    }

    fun getPreconditionErrors(event: CommandEvent<*>) =
        preconditions
            .map { it.evaluate(event) }
            .filterIsInstance<Fail>()
            .map { it.reason }

    val rawInputs = when {
        isPrefixInvocation(content, prefix) -> stripPrefixInvocation(content, prefix)
        content.trimToID() == channel.kord.getSelf().id.toString() -> {
            val embed = config.mentionEmbed ?: return@on

            channel.createEmbed {
                embed.invoke(this, discordContext)
            }

            return@on
        }
        isMentionInvocation(content) -> stripMentionInvocation(content)
        else -> return@on conversationService.handleMessage(message)
    }

    val (_, commandName, actualArgs, _) = rawInputs

    if (commandName.isEmpty()) return@on

    val event = CommandEvent<GenericContainer>(rawInputs, container, discordContext)
    val errors = getPreconditionErrors(event)

    if (errors.isNotEmpty()) {
        val errorMessage = errors.firstOrNull { it.isNotBlank() }

        if (errorMessage != null)
            event.respond(errorMessage)

        return@on
    }

    val command = container[commandName]

    if (command == null) {
        val guild = message.getGuildOrNull()

        return@on CommandRecommender.sendRecommendationEmbed(event, commandName) {
            config.visibilityPredicate(it, author, channel, guild)
        }
    }

    if (message.getGuildOrNull() == null)
        if (command.requiresGuild ?: config.requiresGuild)
            return@on

    config.commandReaction.let {
        message.addReaction(it!!.toReaction())
    }

    command.invoke(actualArgs, event)
}