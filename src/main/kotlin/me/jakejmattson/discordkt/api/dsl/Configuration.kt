@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import dev.kord.core.enableEvent
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.Event
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.NestedDSL
import java.awt.Color

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
 * @property intents Additional gateway intents to register manually.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
data class BotConfiguration(
    val packageName: String,
    val allowMentionPrefix: Boolean,
    val showStartupLog: Boolean,
    val generateCommandDocs: Boolean,
    val recommendCommands: Boolean,
    val enableSearch: Boolean,
    val commandReaction: DiscordEmoji?,
    val theme: Color?,
    val intents: MutableSet<Intent>,
    val entitySupplyStrategy: EntitySupplyStrategy<*>,
    val permissionLevels: List<Enum<*>>,
    val defaultRequiredPermission: Enum<*>,

    internal val prefix: suspend (DiscordContext) -> String,
    internal val mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)?,
) {
    @PublishedApi
    internal inline fun <reified T : Event> enableEvent() {
        intents.addAll(
            Intents {
                this.enableEvent<T>()
            }.values
        )
    }
}

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
 * @property intents Additional gateway intents to register manually.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
data class SimpleConfiguration(var allowMentionPrefix: Boolean = true,
                               var showStartupLog: Boolean = true,
                               var generateCommandDocs: Boolean = true,
                               var recommendCommands: Boolean = true,
                               var enableSearch: Boolean = true,
                               var commandReaction: DiscordEmoji? = Emojis.eyes,
                               var theme: Color? = null,
                               var intents: Set<Intent> = setOf(),
                               var entitySupplyStrategy: EntitySupplyStrategy<*> = EntitySupplyStrategy.cacheWithCachingRestFallback) {
    @PublishedApi
    internal var permissionLevels: List<Enum<*>> = listOf(DefaultPermissions.NONE)

    @PublishedApi
    internal var defaultRequiredPermission: Enum<*> = DefaultPermissions.NONE

    @NestedDSL
    inline fun <reified T : Enum<T>> permissions(defaultRequiredPermission: Enum<T>) {
        this.permissionLevels = enumValues<T>().toList()
        this.defaultRequiredPermission = defaultRequiredPermission

        println(permissionLevels.map { it.name })
    }
}

/**
 * Holds information used to determine if a command has permission to run.
 *
 * @param command The command invoked that needs to check for permission.
 * @param discord The discord instance.
 * @param user The discord user who invoked the command.
 * @param channel The channel that this command was invoked in.
 * @param guild The guild that this command was invoked in.
 */
data class PermissionContext(val command: Command, val discord: Discord, val user: User, val channel: MessageChannel, val guild: Guild?)

interface PermissionSet {
    suspend fun hasPermission(context: PermissionContext): Boolean
}

enum class DefaultPermissions : PermissionSet {
    NONE {
        override suspend fun hasPermission(context: PermissionContext) = true
    }
}