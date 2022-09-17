package me.jakejmattson.discordkt.dsl

import dev.kord.common.kColor
import dev.kord.core.Kord
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
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
    private data class StartupFunctions(var configure: suspend SimpleConfiguration.() -> Unit = { SimpleConfiguration() },
                                        var mentionEmbed: Pair<String?, (suspend EmbedBuilder.(DiscordContext) -> Unit)?> = "info" to defaultMentionEmbed,
                                        var exceptionHandler: suspend DktException<*>.() -> Unit = { exception.printStackTrace() },
                                        var locale: Locale = Language.EN.locale,
                                        var presence: PresenceBuilder.() -> Unit = {},
                                        var onStart: suspend Discord.() -> Unit = {})

    private val startupBundle = StartupFunctions()

    internal suspend fun buildBot() {
        val (configureFun,
            mentionEmbedFun,
            exceptionHandlerFun,
            locale,
            presenceFun,
            startupFun) = startupBundle

        val simpleConfiguration = SimpleConfiguration()
        configureFun.invoke(simpleConfiguration)

        val botConfiguration = with(simpleConfiguration) {
            BotConfiguration(
                packageName = packageName,
                logStartup = logStartup,
                documentCommands = documentCommands,
                theme = theme?.kColor,
                intents = intents + intentsOf<MessageCreateEvent>() + intentsOf<InteractionCreateEvent>(),
                defaultPermissions = defaultPermissions,
                entitySupplyStrategy = entitySupplyStrategy,
                mentionEmbed = mentionEmbedFun,
                exceptionHandler = exceptionHandlerFun
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
     * An embed that will be sent anytime someone (solely) mentions the bot.
     */
    @ConfigurationDSL
    public fun mentionEmbed(slashName: String? = "info", construct: (suspend EmbedBuilder.(DiscordContext) -> Unit)? = defaultMentionEmbed) {
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