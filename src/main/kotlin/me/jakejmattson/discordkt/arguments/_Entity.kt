package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.util.consumeFirst
import me.jakejmattson.discordkt.util.toSnowflakeOrNull

/**
 * An [Argument] that accepts a discord entity.
 */
public interface EntityArgument<Input, Output> : Argument<Input, Output>

/**
 * An [Argument] that accepts a [User].
 */
public interface UserArgument<Output> : EntityArgument<User, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): User? {
        return args.consumeFirst().toSnowflakeOrNull()?.let { discord.kord.getUser(it) }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.author.mention)
}

/**
 * An [Argument] that accepts a [Role].
 */
public interface RoleArgument<Output> : EntityArgument<Role, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Role? {
        val roles = discord.kord.guilds.toList().flatMap { it.roles.toList() }
        val snowflake = args.consumeFirst().toSnowflakeOrNull()
        return roles.find { it.id == snowflake }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("@everyone")
}

/**
 * An [Argument] that accepts a [Channel].
 */
public interface ChannelArgument<Output> : EntityArgument<Channel, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Channel? {
        return args.consumeFirst().toSnowflakeOrNull()?.let { discord.kord.getChannel(it) }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.channel.mention)
}

/**
 * An [Argument] that accepts an [Attachment].
 */
public interface AttachmentArgument<Output> : EntityArgument<Attachment, Output>