package me.jakejmattson.discordkt.api.dsl

import dev.kord.common.annotation.KordPreview
import dev.kord.common.kColor
import dev.kord.core.Kord
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.commands.Command
import me.jakejmattson.discordkt.api.commands.DiscordContext
import me.jakejmattson.discordkt.api.extensions.intentsOf
import me.jakejmattson.discordkt.api.locale.Language
import me.jakejmattson.discordkt.api.locale.Locale
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.services.InjectionService
import me.jakejmattson.discordkt.internal.utils.InternalLogger
import me.jakejmattson.discordkt.internal.utils.Reflection
import me.jakejmattson.discordkt.internal.utils.ReflectionUtils
import java.io.File

@PublishedApi
internal val diService: InjectionService = InjectionService()

internal lateinit var internalLocale: Locale

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [prefix][Bot.prefix],
 * [configure][Bot.configure],
 * [mentionEmbed][Bot.mentionEmbed],
 * [presence][Bot.presence],
 * [localeOf][Bot.localeOf],
 * [onStart][Bot.onStart]
 *
 * @param token Your Discord bot token.
 */
@KordPreview
@ConfigurationDSL
public fun bot(token: String?, configure: suspend Bot.() -> Unit) {
    val packageName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.`package`.name

    if (token.isNullOrEmpty())
        return InternalLogger.fatalError("Missing token!")

    Reflection = ReflectionUtils(packageName)
    val bot = Bot(token, packageName)

    runBlocking {
        bot.configure()
        bot.buildBot()
    }
}

/**
 * Backing class for [bot] function.
 */
public class Bot(private val token: String, private val packageName: String) {
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
        val permissionBundle = PermissionBundle(simpleConfiguration.permissionLevels, simpleConfiguration.commandDefault)

        val botConfiguration = with(simpleConfiguration) {
            BotConfiguration(
                packageName = packageName,
                allowMentionPrefix = allowMentionPrefix,
                showStartupLog = showStartupLog,
                generateCommandDocs = generateCommandDocs,
                recommendCommands = recommendCommands,
                enableSearch = enableSearch,
                commandReaction = commandReaction,
                theme = theme?.kColor,
                intents = intents + intentsOf<MessageCreateEvent>() + intentsOf<InteractionCreateEvent>(),
                entitySupplyStrategy = entitySupplyStrategy,
                prefix = prefixFun,
                mentionEmbed = mentionEmbedFun,
                ignoreIllegalArgumentExceptionInListeners = ignoreIllegalArgumentExceptionInListeners
            )
        }

        val kord = Kord(token) {
            defaultStrategy = botConfiguration.entitySupplyStrategy
        }

        internalLocale = locale

        val discord = object : Discord() {
            override val kord = kord
            override val configuration = botConfiguration
            override val locale = locale
            override val permissions = permissionBundle
            override val commands = mutableListOf<Command>()
            override val preconditions = mutableListOf<Precondition>()
        }

        discord.initCore()

        discord.kord.login {
            presence {
                presenceFun.invoke(this)
            }

            intents = botConfiguration.intents
            startupFun.invoke(discord)
        }
    }

    /**
     * Inject objects into the dependency injection pool.
     */
    @ConfigurationDSL
    public fun inject(vararg injectionObjects: Any): Unit = injectionObjects.forEach { diService.inject(it) }

    /**
     * Read JSON [Data] from a path if it exists; create it otherwise.
     *
     * @param path The file path to load/save the data.
     * @param fallback An instance to be used if the file does not exist.
     */
    @ConfigurationDSL
    public inline fun <reified T : Data> data(path: String, fallback: () -> T): T {
        return readDataOrDefault(File(path), fallback.invoke()).also { inject(it) }
    }

    /**
     * Modify simple configuration options.
     *
     * @sample SimpleConfiguration
     */
    @ConfigurationDSL
    public fun configure(config: suspend SimpleConfiguration.() -> Unit) {
        startupBundle.configure = config
    }

    /**
     * Determine the prefix in a given context.
     */
    @ConfigurationDSL
    public fun prefix(construct: suspend DiscordContext.() -> String) {
        startupBundle.prefix = construct
    }

    /**
     * An embed that will be sent anytime someone (solely) mentions the bot.
     */
    @ConfigurationDSL
    public fun mentionEmbed(construct: suspend EmbedBuilder.(DiscordContext) -> Unit) {
        startupBundle.mentionEmbed = construct
    }

    /**
     * Configure the locale for this bot.
     *
     * @param language The initial [Language] pack.
     */
    @ConfigurationDSL
    public fun localeOf(language: Language, localeBuilder: Locale.() -> Unit) {
        val localeType = language.locale
        localeBuilder.invoke(localeType)
        startupBundle.locale = localeType
    }

    /**
     * Configure the Discord presence for this bot.
     */
    @ConfigurationDSL
    public fun presence(presence: PresenceBuilder.() -> Unit) {
        startupBundle.presence = presence
    }

    /**
     * When setup is complete, execute this block.
     */
    @ConfigurationDSL
    public fun onStart(start: suspend Discord.() -> Unit) {
        startupBundle.onStart = start
    }
}