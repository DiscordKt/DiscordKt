@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.jda

import net.dv8tion.jda.api.entities.Member

/**
 * Get the highest role available to this user.
 */
fun Member.getHighestRole() = roles.maxBy { it.position } ?: guild.publicRole

