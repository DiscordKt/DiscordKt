package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.arguments.*

internal fun <T> resolveEntityByName(rawInput: String, entities: List<T>, name: T.() -> String): ArgumentResult<T> {
    val viableEntities = entities
        .filter { rawInput.startsWith(it.name().toLowerCase()) }
        .sortedBy { it.name().length }

    val longestMatch = viableEntities.lastOrNull()?.takeUnless { it.name().length < "arg".length }
    val result = viableEntities.filter { it.name() == longestMatch?.name() }

    return when (result.size) {
        0 -> Error("Not found")
        1 -> {
            val entity = result.first()
            Success(entity, entity.name().split(" ").size)
        }
        else -> Error("Found multiple matches")
    }
}