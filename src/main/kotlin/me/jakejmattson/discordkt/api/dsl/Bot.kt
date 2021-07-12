package me.jakejmattson.discordkt.api.dsl

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.gateway.Intents
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.locale.Language
import me.jakejmattson.discordkt.api.locale.Locale
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.services.InjectionService

@PublishedApi
internal val diService = InjectionService()

internal lateinit var internalLocale: Locale

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
                                        var prefix: suspend DiscordContext.() -> String = { "" },
                                        var mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)? = null,
                                        var locale: Locale = Language.EN.locale,
                                        var presence: PresenceBuilder.() -> Unit = {},
                                        var onStart: suspend Discord.() -> Unit = {})

    private val startupBundle = StartupFunctions()

    @KordPreview
    internal suspend fun buildBot() {
        val (configureFun,
            prefixFun,
            mentionEmbedFun,
            locale,
            presenceFun,
            startupFun) = startupBundle

        val simpleConfiguration = SimpleConfiguration()
        configureFun.invoke(simpleConfiguration)

        val botConfiguration = with(simpleConfiguration) {
            BotConfiguration(
                packageName = packageName,
                allowMentionPrefix = allowMentionPrefix,
                showStartupLog = showStartupLog,
                generateCommandDocs = generateCommandDocs,
                recommendCommands = recommendCommands,
                enableSearch = enableSearch,
                commandReaction = commandReaction,
                theme = theme,
                intents = intents.toMutableSet(),
                entitySupplyStrategy = entitySupplyStrategy,
                prefix = prefixFun,
                mentionEmbed = mentionEmbedFun,
                permissionLevels = permissionLevels,
                defaultRequiredPermission = defaultRequiredPermission
            )
        }

        botConfiguration.enableEvent<MessageCreateEvent>()
        botConfiguration.enableEvent<ReactionAddEvent>()

        val kord = Kord(token) {
            intents = Intents(botConfiguration.intents)
            defaultStrategy = botConfiguration.entitySupplyStrategy
        }

        internalLocale = locale

        val discord = object : Discord() {
            override val kord = kord
            override val configuration = botConfiguration
            override val locale = locale
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
     * Inject objects into the dependency injection pool.
     */
    @ConfigurationDSL
    fun inject(vararg injectionObjects: Any) = injectionObjects.forEach { diService.inject(it) }

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
     * Configure the locale for this bot.
     *
     * @param language The initial [Language] pack.
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