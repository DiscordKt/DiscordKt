@file:Suppress("unused")

package me.jakejmattson.kutils.api.services

import me.jakejmattson.kutils.internal.utils.diService

class PersistenceService {
    fun save(obj: Any) = diService.saveObject(obj)
}