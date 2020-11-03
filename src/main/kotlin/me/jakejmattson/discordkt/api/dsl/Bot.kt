package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.gateway.Intents
import com.gitlab.kordlib.gateway.builder.PresenceBuilder
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.pluralize
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.listeners.*
import me.jakejmattson.discordkt.internal.services.*
import me.jakejmattson.discordkt.internal.utils.*
import kotlin.system.exitProcess

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
@ConfigurationDSL
suspend fun bot(token: String, operate: suspend Bot.() -> Unit) {
    val path = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.`package`.name
    val bot = Bot(token, path)
    bot.operate()
    bot.buildBot()
}

/**
 * Backing class for [bot] function.
 */
class Bot(private val token: String, private val globalPath: String) {
    private data class StartupFunctions(var configure: suspend SimpleConfiguration.() -> Unit = { SimpleConfiguration() },
                                        var prefix: suspend DiscordContext.() -> String = { "+" },
                                        var mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)? = null,
                                        var permissions: suspend (Command, Discord, User, MessageChannel, Guild?) -> Boolean = { _, _, _, _, _ -> true },
                                        var presence: PresenceBuilder.() -> Unit = {},
                                        var onStart: suspend Discord.() -> Unit = {})

    private val startupBundle = StartupFunctions()

    private suspend fun initCore(discord: Discord) {
        diService.inject(discord)
        val showStartupLog = discord.configuration.showStartupLog
        val generateCommandDocs = discord.configuration.generateCommandDocs
        val header = "----- DiscordKt ${discord.versions.library} -----"

        if (showStartupLog)
            InternalLogger.log(header)

        val dataSize = registerData()
        val services = registerServices()

        ReflectionUtils.registerFunctions(globalPath, discord)
        registerReactionListener(discord.api)
        registerCommandListener(discord)

        val commandSets = discord.commands.groupBy { it.category }.keys.size

        if (showStartupLog) {
            InternalLogger.log(commandSets.pluralize("CommandSet") + " -> " + discord.commands.size.pluralize("Command"))
            InternalLogger.log(dataSize.pluralize("Data"))
            InternalLogger.log(services.size.pluralize("Service"))
            InternalLogger.log(discord.preconditions.size.pluralize("Precondition"))
        }

        registerHelpCommand(discord)

        if (generateCommandDocs)
            createDocumentation(discord.commands)

        Validator.validateCommandMeta(discord.commands)

        if (showStartupLog)
            InternalLogger.log("-".repeat(header.length))
    }

    internal suspend fun buildBot() {
        val (configureFun,
            prefixFun,
            mentionEmbedFun,
            permissionsFun,
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
                commandReaction = commandReaction,
                theme = theme,
                intents = intents,
                prefix = prefixFun,
                mentionEmbed = mentionEmbedFun,
                permissions = permissionsFun
            )
        }

        val kord = Kord(token) {
            intents {
                botConfiguration.intents.forEach {
                    + it
                }
            }
        }

        val discord = buildDiscordClient(kord, botConfiguration)

        initCore(discord)
        discord.api.login {
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

    private fun registerServices() = ReflectionUtils.detectClassesWith<Service>(globalPath).apply { diService.buildAllRecursively(this) }
    private fun registerHelpCommand(discord: Discord) = discord.commands["Help"]
        ?: produceHelpCommand().register(discord)

    private fun registerData() = ReflectionUtils.detectSubtypesOf<Data>(globalPath)
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
        }.size
}