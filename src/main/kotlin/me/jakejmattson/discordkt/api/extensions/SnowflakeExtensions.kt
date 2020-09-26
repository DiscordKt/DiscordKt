package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.common.entity.Snowflake

/**
 * Convert an ID to a Snowflake.
 */
fun Long.toSnowflake() = Snowflake(this)

/**
 * Convert an ID or mention to a Snowflake.
 */
fun String.toSnowflake() = Snowflake(this)

/**
 * Convert an ID or mention to a Snowflake.
 */
fun String.toSnowflakeOrNull() = trimToID().toLongOrNull()?.let { Snowflake(it) }