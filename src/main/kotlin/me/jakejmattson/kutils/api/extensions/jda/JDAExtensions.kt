@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.jda

import net.dv8tion.jda.api.JDA

/**
 * Retrieve a JDA entity from a snowflake.
 */
fun JDA.tryRetrieveSnowflake(action: (JDA) -> Any?) =
    try {
        action(this)
    } catch (e: RuntimeException) {
        null
    }