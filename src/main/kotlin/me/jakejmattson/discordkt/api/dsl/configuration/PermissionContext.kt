package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.User
import me.jakejmattson.discordkt.api.dsl.command.Command

/**
 * @suppress Used in sample
 */
data class PermissionContext(val command: Command, val user: User, val channel: MessageChannelBehavior)