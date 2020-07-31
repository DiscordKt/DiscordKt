package me.jakejmattson.discordkt.api.dsl.configuration

import me.jakejmattson.discordkt.api.dsl.command.Command
import net.dv8tion.jda.api.entities.*

/**
 * @suppress Used in sample
 */
data class VisibilityContext(val command: Command, val user: User, val channel: MessageChannel, val guild: Guild?)