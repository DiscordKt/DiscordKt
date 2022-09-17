@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intents
import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.commands.DiscordContext

/**
 * Contains all properties configured when the bot is created.
 *
 * @property packageName The detected package name for the bot.
 * @property logStartup Display log information when the bot starts.
 * @property documentCommands Generate a markdown file of command info.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents Additional gateway [Intents] to register manually.
 * @property defaultPermissions The [Permissions] used to restrict command usage.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
public data class BotConfiguration(
    val packageName: String,
    val logStartup: Boolean,
    val documentCommands: Boolean,
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
 * @property logStartup Display log information when the bot starts.
 * @property documentCommands Generate a markdown file of command info.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents Additional gateway [Intents] to register manually.
 * @property defaultPermissions The [Permissions] used to restrict command usage.
 * @property entitySupplyStrategy [EntitySupplyStrategy] for use in Kord cache.
 */
public data class SimpleConfiguration(
    var logStartup: Boolean = true,
    var documentCommands: Boolean = true,
    var theme: java.awt.Color? = null,
    var intents: Intents = Intents.none,
    var defaultPermissions: Permissions = Permissions(Permission.UseApplicationCommands),
    var entitySupplyStrategy: EntitySupplyStrategy<*> = EntitySupplyStrategy.cacheWithCachingRestFallback,
)