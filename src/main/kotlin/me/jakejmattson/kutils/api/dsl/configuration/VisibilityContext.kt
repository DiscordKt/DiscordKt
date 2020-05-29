package me.jakejmattson.kutils.api.dsl.configuration

import me.aberrantfox.kutils.api.dsl.command.Command
import net.dv8tion.jda.api.entities.*

data class VisibilityContext(val command: Command, val user: User, val channel: MessageChannel, val guild: Guild?)