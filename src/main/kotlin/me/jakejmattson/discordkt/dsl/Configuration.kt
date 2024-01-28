@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intents
import dev.kord.gateway.NONE
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.commands.DiscordContext

/**
 * Contains all properties configured when the bot is created.
 *
 * @property packageName The detected package name for the bot.
 * @property mentionAsPrefix Use the bot mention (@Bot) as a prefix.
 * @property logStartup Display log information when the bot starts.
 * @property documentCommands Generate a markdown file of command info.
 * @property recommendCommands Recommend the closest command name to an invalid one.
 * @property searchCommands Allow command searching with 'search <command name>'.
 * @property deleteInvocation Delete a command invocation message after execution.
 * @property dualRegistry Allow invocation of a slash command as a text command.
 * @property commandReaction A reaction added to the command invocation message.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents Additional gateway [Intents] to register manually.
 * @property defaultPermissions The [Permissions] used to restrict command usage.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
public data class BotConfiguration(
    val packageName: String,
    val mentionAsPrefix: Boolean,
    val logStartup: Boolean,
    val documentCommands: Boolean,
    val recommendCommands: Boolean,
    val searchCommands: Boolean,
    val deleteInvocation: Boolean,
    val dualRegistry: Boolean,
    val commandReaction: DiscordEmoji?,
    val theme: Color?,
    val intents: Intents,
    val defaultPermissions: Permissions,
    val entitySupplyStrategy: EntitySupplyStrategy<*>,

    internal val prefix: suspend (DiscordContext) -> String,
    internal val mentionEmbed: Pair<String?, (suspend EmbedBuilder.(DiscordContext) -> Unit)?>,

    @PublishedApi
    internal val exceptionHandler: suspend DktException<*>.() -> Unit
)

/**
 * Simple configuration values that don't require a builder.
 *
 * @property mentionAsPrefix Use the bot mention (@Bot) as a prefix.
 * @property logStartup Display log information when the bot starts.
 * @property documentCommands Generate a markdown file of command info.
 * @property recommendCommands Recommend the closest command name to an invalid one.
 * @property searchCommands Allow command searching with 'search <command name>'.
 * @property deleteInvocation Delete a command invocation message after execution.
 * @property dualRegistry Allow invocation of a slash command as a text command.
 * @property commandReaction A reaction added to the command invocation message.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents Additional gateway [Intents] to register manually.
 * @property defaultPermissions The [Permissions] used to restrict command usage.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
public data class SimpleConfiguration(
    var mentionAsPrefix: Boolean = true,
    var logStartup: Boolean = true,
    var documentCommands: Boolean = true,
    var recommendCommands: Boolean = true,
    var searchCommands: Boolean = true,
    var deleteInvocation: Boolean = true,
    var dualRegistry: Boolean = true,
    var commandReaction: DiscordEmoji? = Emojis.eyes,
    var theme: java.awt.Color? = null,
    var intents: Intents = Intents.NONE,
    var defaultPermissions: Permissions = Permissions(Permission.UseApplicationCommands),
    var entitySupplyStrategy: EntitySupplyStrategy<*> = EntitySupplyStrategy.cacheWithCachingRestFallback,
)