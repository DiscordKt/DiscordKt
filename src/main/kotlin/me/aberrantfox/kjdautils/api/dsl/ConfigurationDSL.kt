package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User

data class KJDAConfiguration(val token: String = "",

                             var prefix: String = "+",
                             var commandPath: String = "",
                             var deleteOnInvocation: Boolean = true,
                             var visibilityPredicate: (command: String, User, MessageChannel, Guild?) -> Boolean= { _, _, _, _ -> true })