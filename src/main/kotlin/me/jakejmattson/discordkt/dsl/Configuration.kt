@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.common.Color
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intents
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.commands.DiscordContext

/**
 * Contains all properties configured when the bot is created.
 *
 * @property packageName The detected package name for the bot.
 * @property allowMentionPrefix Uses the bot mention (@Bot) as a prefix.
 * @property showStartupLog Displays log information when the bot starts.
 * @property generateCommandDocs Generates a markdown file of command info.
 * @property recommendCommands Recommends the closest command name when an invalid one is attempted.
 * @property enableSearch Allows searching for a command by typing 'search <command name>'.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents Additional gateway [Intents] to register manually.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
public data class BotConfiguration(
    val packageName: String,
    val allowMentionPrefix: Boolean,
    val showStartupLog: Boolean,
    val generateCommandDocs: Boolean,
    val recommendCommands: Boolean,
    val enableSearch: Boolean,
    val commandReaction: DiscordEmoji?,
    val theme: Color?,
    val intents: Intents,
    val entitySupplyStrategy: EntitySupplyStrategy<*>,

    internal val prefix: suspend (DiscordContext) -> String,
    internal val mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)?,

    @PublishedApi
    internal val exceptionHandler: suspend DktException<*>.() -> Unit
)

/**
 * Holds all basic configuration options.
 *
 * @property allowMentionPrefix Uses the bot mention (@Bot) as a prefix.
 * @property showStartupLog Displays log information when the bot starts.
 * @property generateCommandDocs Generates a markdown file of command info.
 * @property recommendCommands Recommends the closest command name when an invalid one is attempted.
 * @property enableSearch Allows searching for a command by typing 'search <command name>'.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents Additional gateway [Intents] to register manually.
 * @property permissions The [PermissionSet] used to restrict command usage.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
public data class SimpleConfiguration(
    var allowMentionPrefix: Boolean = true,
    var showStartupLog: Boolean = true,
    var generateCommandDocs: Boolean = true,
    var recommendCommands: Boolean = true,
    var enableSearch: Boolean = true,
    var commandReaction: DiscordEmoji? = Emojis.eyes,
    var theme: java.awt.Color? = null,
    var intents: Intents = Intents.none,
    var permissions: PermissionSet = DefaultPermissions,
    var entitySupplyStrategy: EntitySupplyStrategy<*> = EntitySupplyStrategy.cacheWithCachingRestFallback,
)

private object DefaultPermissions : PermissionSet {
    val EVERYONE = permission("Everyone") { roles(guild!!.everyoneRole.id) }
    override val hierarchy: List<Permission> = listOf(EVERYONE)
    override val commandDefault: Permission = EVERYONE
}