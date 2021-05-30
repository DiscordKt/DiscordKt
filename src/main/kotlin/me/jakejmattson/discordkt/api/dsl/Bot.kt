package me.jakejmattson.discordkt.api.dsl

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.gateway.Intents
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.services.InjectionService

@PublishedApi
internal val diService = InjectionService()

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][Bot.configure],
 * [prefix][Bot.prefix],
 * [mentionEmbed][Bot.mentionEmbed],
 * [permissions][Bot.permissions],
 * [presence][Bot.presence]
 *
 * @param token Your Discord bot token.
 */
@KordPreview
@ConfigurationDSL
fun bot(token: String, configure: suspend Bot.() -> Unit) {
    val packageName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.`package`.name
    val bot = Bot(token, packageName)

    runBlocking {
        bot.configure()
        bot.buildBot()
    }
}

/**
 * Backing class for [bot] function.
 */
class Bot(private val token: String, private val packageName: String) {
    private data class StartupFunctions(var configure: suspend SimpleConfiguration.() -> Unit = { SimpleConfiguration() },
                                        var prefix: suspend DiscordContext.() -> String = { "+" },
                                        var mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)? = null,
                                        var permissions: suspend (Command, Discord, User, MessageChannel, Guild?) -> Boolean = { _, _, _, _, _ -> true },
                                        var locale: Locale = Language.EN.locale,
                                        var presence: PresenceBuilder.() -> Unit = {},
                                        var onStart: suspend Discord.() -> Unit = {})

    private val startupBundle = StartupFunctions()

    @KordPreview
    internal suspend fun buildBot() {
        val (configureFun,
            prefixFun,
            mentionEmbedFun,
            permissionsFun,
            localeType,
            presenceFun,
            startupFun) = startupBundle

        val simpleConfiguration = SimpleConfiguration()
        configureFun.invoke(simpleConfiguration)

        val botConfiguration = with(simpleConfiguration) {
            BotConfiguration(
                allowMentionPrefix = allowMentionPrefix,
                showStartupLog = showStartupLog,
                generateCommandDocs = generateCommandDocs,
                recommendCommands = recommendCommands,
                enableSearch = enableSearch,
                commandReaction = commandReaction,
                theme = theme,
                intents = intents.toMutableSet(),
                packageName = packageName,
                prefix = prefixFun,
                mentionEmbed = mentionEmbedFun,
                permissions = permissionsFun
            )
        }

        botConfiguration.enableEvent<MessageCreateEvent>()
        botConfiguration.enableEvent<ReactionAddEvent>()

        val kord = Kord(token) {
            intents = Intents(botConfiguration.intents)
        }

        val discord = object : Discord() {
            override val kord = kord
            override val configuration = botConfiguration
            override val locale = localeType
            override val commands = mutableListOf<Command>()
            override val preconditions = mutableListOf<Precondition>()
        }

        discord.initCore()

        discord.kord.login {
            presenceFun.invoke(this)
            startupFun.invoke(discord)
        }
    }

    /**
     * Modify simple configuration options.
     *
     * @sample SimpleConfiguration
     */
    @ConfigurationDSL
    fun configure(config: suspend SimpleConfiguration.() -> Unit) {
        startupBundle.configure = config
    }

    /**
     * Inject objects into the dependency injection pool.
     */
    @ConfigurationDSL
    fun inject(vararg injectionObjects: Any) = injectionObjects.forEach { diService.inject(it) }

    /**
     * Determine the prefix in a given context.
     */
    @ConfigurationDSL
    fun prefix(construct: suspend DiscordContext.() -> String) {
        startupBundle.prefix = construct
    }

    /**
     * An embed that will be sent anytime someone (solely) mentions the bot.
     */
    @ConfigurationDSL
    fun mentionEmbed(construct: suspend EmbedBuilder.(DiscordContext) -> Unit) {
        startupBundle.mentionEmbed = construct
    }

    /**
     * Determine if the given command has permission to be run in this context.
     *
     * @sample PermissionContext
     */
    @ConfigurationDSL
    fun permissions(predicate: suspend PermissionContext.() -> Boolean) {
        startupBundle.permissions = { command, discord, user, messageChannel, guild ->
            val context = PermissionContext(command, discord, user, messageChannel, guild)
            predicate.invoke(context)
        }
    }

    /**
     * Configure the localization for this bot.
     */
    @ConfigurationDSL
    fun localeOf(language: Language, localeBuilder: Locale.() -> Unit) {
        val localeType = language.locale
        localeBuilder.invoke(localeType)
        startupBundle.locale = localeType
    }

    /**
     * Configure the Discord presence for this bot.
     */
    @ConfigurationDSL
    fun presence(presence: PresenceBuilder.() -> Unit) {
        startupBundle.presence = presence
    }

    /**
     * When setup is complete, execute these tasks.
     */
    @ConfigurationDSL
    fun onStart(start: suspend Discord.() -> Unit) {
        startupBundle.onStart = start
    }
}