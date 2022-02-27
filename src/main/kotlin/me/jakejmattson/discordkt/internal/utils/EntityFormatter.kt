package me.jakejmattson.discordkt.internal.utils

import dev.kord.core.entity.*
import dev.kord.core.entity.channel.GuildMessageChannel
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.commands.Command
import java.awt.Color

public fun <T> stringify(entity: T): String =
    when (entity) {
        //Discord entities
        is GuildMessageChannel -> entity.id.toString()
        is Attachment -> entity.filename
        is Guild -> entity.id.toString()
        is Role -> entity.id.toString()
        is User -> entity.id.toString()

        //DiscordKt
        is Command -> entity.name
        is Success<*> -> stringify(entity.result)
        is Error<*> -> entity.error

        //Standard Library
        is Color -> with(entity) { "#%02X%02X%02X".format(red, green, blue) }

        else -> entity.toString()
    }
