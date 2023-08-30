package me.jakejmattson.discordkt.dsl

import dev.kord.common.kColor
import dev.kord.core.Kord
import dev.kord.core.builder.kord.KordBuilder
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.commands.SubCommandSet
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.services.InjectionService
import me.jakejmattson.discordkt.internal.utils.InternalLogger
import me.jakejmattson.discordkt.internal.utils.Reflection
import me.jakejmattson.discordkt.internal.utils.ReflectionUtils
import me.jakejmattson.discordkt.locale.Language
import me.jakejmattson.discordkt.locale.Locale
import me.jakejmattson.discordkt.util.*
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

private val defaultMentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit) = {
    val discord = it.discord
    val properties = discord.properties
    val bot = properties.bot

    title = "${bot.name} ${bot.version}"
    description = bot.description
    color = discord.configuration.theme
    addInlineField("Source", bot.url?.let { "[GitHub]($it)" } ?: "Closed")
    addInlineField("Ping", discord.kord.gateway.averagePing?.toString() ?: "Unknown")
    addInlineField("Startup", TimeStamp.at(properties.startup, TimeStyle.RELATIVE))
    thumbnail(discord.kord.getSelf().pfpUrl)
    footer(properties.library.toString())
}

/**
 * Backing class for [bot] function.
 */
public class Bot(private val token: String, private val packageName: String) {
    private data class StartupFunctions(
        var configure: suspend SimpleConfiguration.() -> Unit = { SimpleConfiguration() },
        var prefix: suspend DiscordContext.() -> String = { "!" },
        var mentionEmbed: Pair<String?, (suspend EmbedBuilder.(DiscordContext) -> Unit)?> = "info" to defaultMentionEmbed,
        var exceptionHandler: suspend DktException<*>.() -> Unit = { exception.printStackTrace() },
        var kordBuilder: KordBuilder.() -> Unit = {},
        var locale: Locale = Language.EN.locale,
        var presence: PresenceBuilder.() -> Unit = {},
        var onStart: suspend Discord.() -> Unit = {}
    )

    private val startupBundle = StartupFunctions()

    internal suspend fun buildBot() {
        val (configureFun,
            prefixFun,
            mentionEmbedFun,
            exceptionHandlerFun,
            kordBuilder,
            locale,
            presenceFun,
            startupFun) = startupBundle

        val simpleConfiguration = SimpleConfiguration()
        configureFun.invoke(simpleConfiguration)

        val botConfiguration = with(simpleConfiguration) {
            BotConfiguration(
                packageName = packageName,
                mentionAsPrefix = mentionAsPrefix,
                logStartup = logStartup,
                documentCommands = documentCommands,
                recommendCommands = recommendCommands,
                searchCommands = searchCommands,
                deleteInvocation = deleteInvocation,
                dualRegistry = dualRegistry,
                commandReaction = commandReaction,
                theme = theme?.kColor,
                intents = intents + intentsOf<InteractionCreateEvent>(),
                defaultPermissions = defaultPermissions,
                entitySupplyStrategy = entitySupplyStrategy,
                prefix = prefixFun,
                mentionEmbed = mentionEmbedFun,
                exceptionHandler = exceptionHandlerFun
            )
        }

        val kord = Kord(token) {
            defaultStrategy = botConfiguration.entitySupplyStrategy
            kordBuilder.invoke(this)
        }

        internalLocale = locale

        val discord = object : Discord() {
            override val kord = kord
            override val configuration = botConfiguration
            override val locale = locale
            override val commands = mutableListOf<Command>()
            override val subcommands = mutableListOf<SubCommandSet>()
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
     * @sample me.jakejmattson.discordkt.dsl.SimpleConfiguration
     */
    @ConfigurationDSL
    public fun configure(config: suspend SimpleConfiguration.() -> Unit) {
        startupBundle.configure = config
    }

    /**
     * Determine the prefix in a given context.
     * Default prefix: '!'.
     * **Warning!: Don't set empty string!**
     * https://github.com/DiscordKt/DiscordKt/issues/32
     */
    @ConfigurationDSL
    public fun prefix(construct: suspend DiscordContext.() -> String) {
        startupBundle.prefix = construct
    }

    /**
     * An embed that will be sent anytime someone (solely) mentions the bot.
     */
    @ConfigurationDSL
    public fun mentionEmbed(
        slashName: String? = "info",
        construct: (suspend EmbedBuilder.(DiscordContext) -> Unit)? = defaultMentionEmbed
    ) {
        startupBundle.mentionEmbed = slashName to construct
    }

    /**
     * Function to handle any exception that occur during runtime.
     */
    @ConfigurationDSL
    public fun onException(handler: suspend DktException<*>.() -> Unit) {
        startupBundle.exceptionHandler = handler
    }

    /**
     * Configure the locale for this bot.
     * You can find predefined locales in the [Language]
     *
     * @param locale The initial [Locale].
     */
    @ConfigurationDSL
    public fun localeOf(locale: Locale, localeBuilder: Locale.() -> Unit) {
        localeBuilder.invoke(locale)
        startupBundle.locale = locale
    }

    /**
     * Configure Kord options via the [KordBuilder].
     */
    @ConfigurationDSL
    public fun kord(builder: KordBuilder.() -> Unit) {
        startupBundle.kordBuilder = builder
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