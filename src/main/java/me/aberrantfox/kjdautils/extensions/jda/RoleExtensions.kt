package me.aberrantfox.hotbot.extensions.jda

import net.dv8tion.jda.core.entities.Role


fun Role.isEqualOrHigherThan(other: Role?) = if(other == null) false else this.position >= other.position