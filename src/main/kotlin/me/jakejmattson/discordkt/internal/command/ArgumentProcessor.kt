package me.jakejmattson.discordkt.internal.command

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.bundleToContainer
import me.jakejmattson.discordkt.commands.DiscordContext

internal suspend fun transformArgs(args: List<Pair<Argument<*, *>, Any?>>, context: DiscordContext): Result<*> {
    val transformations = args.map { (rawArg, value) ->
        if (value == null) {
            require(rawArg is OptionalArg<*, *, *>) { "Missing required arguments" }
            runBlocking { Success(rawArg.default.invoke(context)) }
        } else {
            val arg = when (rawArg) {
                is OptionalArg<*, *, *> -> rawArg.type
                else -> rawArg
            }

            when (arg) {
                //Simple
                is StringArgument -> arg.transform(value as String, context)
                is IntegerArgument -> arg.transform(value as Int, context)
                is DoubleArgument -> arg.transform(value as Double, context)
                is BooleanArgument -> arg.transform(value as Boolean, context)

                //Entity
                is UserArgument -> arg.transform(value as User, context)
                is RoleArgument -> arg.transform(value as Role, context)
                is ChannelArgument -> arg.transform(value as Channel, context)
                is AttachmentArgument -> arg.transform(value as Attachment, context)

                //Unknown
                else -> Success(value)
            }
        }
    }

    return transformations.firstOrNull { it is Error }
        ?: Success(bundleToContainer(transformations.map { (it as Success<*>).result }))
}