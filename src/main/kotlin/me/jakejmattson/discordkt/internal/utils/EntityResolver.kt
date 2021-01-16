package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.arguments.Success

internal fun <T> resolveEntityByName(args: List<String>, entities: List<T>, name: T.() -> String): ArgumentResult<T> {
    val rawInput = args.joinToString(" ").toLowerCase()

    val viableEntities = entities
        .filter { rawInput.startsWith(it.name().toLowerCase()) }
        .sortedBy { it.name().length }

    val longestMatch = viableEntities.lastOrNull()?.name()
    val result = viableEntities.filter { it.name() == longestMatch }

    return when (result.size) {
        0 -> Error("Not found")
        1 -> {
            val entity = result.first()
            Success(entity, entity.name().split(" ").size)
        }
        else -> Error("Found multiple matches")
    }
}