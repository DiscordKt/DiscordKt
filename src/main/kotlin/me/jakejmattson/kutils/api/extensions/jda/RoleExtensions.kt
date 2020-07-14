@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.jda

import net.dv8tion.jda.api.entities.Role

/**
 * Compare two roles by their position.
 */
fun Role.isEqualOrHigherThan(other: Role) = position >= other.position