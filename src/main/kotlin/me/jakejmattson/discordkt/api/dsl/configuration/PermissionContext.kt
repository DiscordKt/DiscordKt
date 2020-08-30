package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.*
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.Command

/**
 * @suppress Used in sample
 */
data class PermissionContext(val command: Command, val discord: Discord, val user: User, val channel: MessageChannelBehavior, val guild: Guild?)