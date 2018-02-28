package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.api.types.GuildID

data class KJDAConfiguration(var token: String = "",
                             var ownerID: String = "",
                             var prefix: String = "+",
                             var guildID: GuildID,
                             var commandPath: String = "")