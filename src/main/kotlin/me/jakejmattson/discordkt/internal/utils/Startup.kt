package me.jakejmattson.discordkt.internal.utils

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.api.dsl.configuration.*
import me.jakejmattson.discordkt.api.dsl.data.Data
import me.jakejmattson.discordkt.api.dsl.preconditions.Precondition
import me.jakejmattson.discordkt.api.extensions.pluralize
import me.jakejmattson.discordkt.api.services.ConversationService
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.listeners.*
import me.jakejmattson.discordkt.internal.services.*
import java.awt.Color
import kotlin.system.exitProcess

@PublishedApi
internal val diService = InjectionService()

/**
 * Backing class for [bot][me.jakejmattson.discordkt.api.dsl.bot] function.
 */
class Bot(private val token: String, private val globalPath: String) {
    private data class StartupFunctions(var configure: BotConfiguration.(Discord) -> Unit = { BotConfiguration() },
                                        var injection: InjectionConfiguration.() -> Unit = { InjectionConfiguration() },
                                        var logging: LoggingConfiguration.() -> Unit = { LoggingConfiguration() })

    private val startupBundle = StartupFunctions()
    private val botConfiguration = BotConfiguration()

    private suspend fun initCore(discord: Discord, loggingConfiguration: LoggingConfiguration) {
        diService.inject(discord)

        InternalLogger.shouldLogStartup = loggingConfiguration.showStartupLog
        val header = "------- DiscordKt ${discord.versions.library} -------"
        InternalLogger.startup(header)

        val data = registerData()
        val conversationService = ConversationService(discord).apply { diService.inject(this) }
        val services = registerServices()
        val preconditions = buildPreconditions().sortedBy { it.priority }

        ReflectionUtils.fireRegisteredFunctions(globalPath, discord)

        discord.commands["Help"] ?: produceHelpCommand(Color.BLUE).registerCommands(discord)

        registerCommandListener(discord, preconditions)
        registerReactionListener(discord.api, conversationService)

        InternalLogger.startup(data.size.pluralize("Data"))
        InternalLogger.startup(services.size.pluralize("Service"))
        InternalLogger.startup(preconditions.size.pluralize("Precondition"))

        conversationService.registerConversations(globalPath)

        if (loggingConfiguration.generateCommandDocs)
            createDocumentation(discord.commands)

        Validator.validateCommandMeta(discord.commands)

        InternalLogger.startup("-".repeat(header.length))
    }

    internal suspend fun buildBot() {
        val (configureFun, injectionFun, loggingFun) = startupBundle

        val loggingConfiguration = LoggingConfiguration()
        loggingFun.invoke(loggingConfiguration)

        val injection = InjectionConfiguration()
        injectionFun.invoke(injection)

        val discord = buildDiscordClient(Kord(token), botConfiguration)
        initCore(discord, loggingConfiguration)
        configureFun.invoke(botConfiguration, discord)

        discord.api.login()
    }

    /**
     * Modify core configuration.
     *
     * @sample BotConfiguration
     */
    @ConfigurationDSL
    fun configure(config: BotConfiguration.(Discord) -> Unit) {
        startupBundle.configure = config
    }

    /**
     * Inject objects into the dependency injection pool.
     *
     * @sample InjectionConfiguration
     */
    @ConfigurationDSL
    fun injection(config: InjectionConfiguration.() -> Unit) {
        startupBundle.injection = config
    }

    /**
     * Modify logging configuration.
     *
     * @sample LoggingConfiguration
     */
    @ConfigurationDSL
    fun logging(config: LoggingConfiguration.() -> Unit) {
        startupBundle.logging = config
    }

    /**
     * Determine the prefix in a given context.
     */
    @ConfigurationDSL
    fun prefix(construct: (DiscordContext) -> String) {
        botConfiguration.prefix = construct
    }

    /**
     * An embed that will be sent anytime someone (solely) mentions the bot.
     */
    @ConfigurationDSL
    fun mentionEmbed(construct: EmbedBuilder.(DiscordContext) -> Unit) {
        botConfiguration.mentionEmbed = construct
    }

    /**
     * Determine if the given command has permission to be run in this context.
     *
     * @sample PermissionContext
     */
    @ConfigurationDSL
    fun permissions(predicate: (PermissionContext) -> Boolean = { _ -> true }) {
        botConfiguration.permissions = { command, user, messageChannel, guild ->
            val context = PermissionContext(command, user, messageChannel, guild)
            predicate.invoke(context)
        }
    }

    /**
     * Block to set global color constants, specifically for embeds.
     *
     * @sample ColorConfiguration
     */
    @ConfigurationDSL
    fun colors(construct: ColorConfiguration.() -> Unit) {
        val colors = ColorConfiguration()
        colors.construct()
    }

    private fun registerServices() = ReflectionUtils.detectClassesWith<Service>(globalPath).apply { diService.buildAllRecursively(this) }
    private fun buildPreconditions() = ReflectionUtils.detectSubtypesOf<Precondition>(globalPath).map { diService.invokeConstructor(it) }

    private fun registerData() = ReflectionUtils
        .detectSubtypesOf<Data>(globalPath)
        .map {
            val default = it.getConstructor().newInstance()

            val data = with(default) {
                if (file.exists()) {
                    readFromFile()
                } else {
                    writeToFile()

                    if (killIfGenerated) {
                        InternalLogger.error("Please fill in the following file before re-running: ${file.absolutePath}")
                        exitProcess(-1)
                    }

                    this
                }
            }

            diService.inject(data)
        }
}