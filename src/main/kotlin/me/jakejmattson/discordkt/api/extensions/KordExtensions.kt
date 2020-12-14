@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intent

/**
 * Convert a Long ID to a Snowflake.
 */
fun Long.toSnowflake() = Snowflake(this)

/**
 * Convert a String ID or mention to a Snowflake.
 */
fun String.toSnowflake() = Snowflake(this)

/**
 * Convert an ID or mention to a Snowflake.
 */
fun String.toSnowflakeOrNull() = trimToID().toLongOrNull()?.let { Snowflake(it) }

/**
 * Combine two intents into a set.
 */
operator fun Intent.plus(intent: Intent) = mutableSetOf(this, intent)