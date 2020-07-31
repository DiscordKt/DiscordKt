@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions.jda

import net.dv8tion.jda.api.entities.Role

/**
 * Define the comparison operator for roles.
 */
operator fun Role.compareTo(other: Role) = position.compareTo(other.position)