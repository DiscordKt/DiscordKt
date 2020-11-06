package me.jakejmattson.discordkt.internal.utils

import com.gitlab.kordlib.core.event.*
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.channel.*
import com.gitlab.kordlib.core.event.guild.*
import com.gitlab.kordlib.core.event.message.*
import com.gitlab.kordlib.core.event.role.*
import com.gitlab.kordlib.gateway.*
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.arguments.EitherArg
import me.jakejmattson.discordkt.api.dsl.Command
import kotlin.reflect.KClass

private val reactionEvents: List<KClass<out Event>> = listOf(ReactionAddEvent::class, ReactionRemoveEvent::class, ReactionRemoveAllEvent::class, ReactionRemoveEmojiEvent::class)

@OptIn(PrivilegedIntent::class)
private val requiredIntents: Map<Intent, List<KClass<out Event>>> = mapOf(
    Intent.DirectMessages to listOf(ChannelCreateEvent::class, ChannelDeleteEvent::class, MessageCreateEvent::class, MessageDeleteEvent::class),
    Intent.DirectMessageTyping to listOf(TypingStartEvent::class),
    Intent.DirectMessagesReactions to reactionEvents,

    Intent.GuildMessages to listOf(MessageCreateEvent::class, MessageUpdateEvent::class, MessageDeleteEvent::class, MessageBulkDeleteEvent::class),
    Intent.GuildMessageTyping to listOf(TypingStartEvent::class),
    Intent.GuildMessageReactions to reactionEvents,

    Intent.GuildBans to listOf(BanAddEvent::class, BanRemoveEvent::class),
    Intent.GuildEmojis to listOf(EmojisUpdateEvent::class),
    Intent.GuildIntegrations to listOf(IntegrationsUpdateEvent::class),
    Intent.GuildInvites to listOf(InviteCreateEvent::class, InviteDeleteEvent::class),
    Intent.GuildMembers to listOf(MemberJoinEvent::class, MemberLeaveEvent::class, MemberUpdateEvent::class),
    Intent.GuildPresences to listOf(PresenceUpdateEvent::class),
    Intent.GuildVoiceStates to listOf(VoiceStateUpdateEvent::class),
    Intent.GuildWebhooks to listOf(WebhookUpdateEvent::class),
    Intent.Guilds to listOf(GuildCreateEvent::class, GuildDeleteEvent::class, RoleCreateEvent::class, RoleUpdateEvent::class,
        RoleDeleteEvent::class, ChannelCreateEvent::class, ChannelUpdateEvent::class, ChannelDeleteEvent::class, ChannelPinsUpdateEvent::class)
)

@PublishedApi
internal object Validator {
    fun validateIntent(discord: Discord, event: KClass<out Event>) {
        val enabledIntents = discord.configuration.intents

        val validIntents = requiredIntents.filter { (_, events) ->
            event in events
        }.map { it.key }

        if (validIntents.none { it in enabledIntents })
            InternalLogger.error("${event.simplerName} missing intent (${validIntents.joinToString("; ") { it.name }})")
    }

    fun validateCommandMeta(commands: MutableList<Command>) {
        val duplicates = commands
            .flatMap { it.names }
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .map { it.key }
            .joinToString { "\"$it\"" }

        if (duplicates.isNotEmpty())
            InternalLogger.error("Found commands with duplicate names: $duplicates")

        commands.forEach { command ->
            val args = command.arguments
            val commandName = command.names.first()

            if (command.names.any { it.isBlank() })
                InternalLogger.error("Found command with blank name in CommandSet ${command.category}")
            else {
                val spaces = command.names.filter { " " in it }

                if (spaces.isNotEmpty())
                    InternalLogger.error("Found command name with spaces: ${spaces.joinToString { "\"$it\"" }}")
            }

            args.filterIsInstance<EitherArg<*, *>>().forEach {
                if (it.left == it.right) {
                    val arg = it.left::class.simplerName
                    InternalLogger.error("Detected EitherArg with identical args ($arg) in command: $commandName")
                }
            }

            if (command.isFlexible) {
                if (args.size < 2)
                    InternalLogger.error("Flexible commands must accept at least 2 arguments ($commandName)")
                else {
                    val actualCount = args.size
                    val distinctCount = args.distinct().size

                    if (distinctCount != actualCount)
                        InternalLogger.error("Flexible commands must accept distinct arguments ($commandName)")
                }
            }
        }
    }
}