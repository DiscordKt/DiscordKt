package me.jakejmattson.kutils.api.dsl.configuration

import me.jakejmattson.kutils.internal.utils.diService

data class InjectionConfiguration(var enableScriptEngineService: Boolean = false) {
    fun inject(vararg injectionObjects: Any) = injectionObjects.forEach { diService.addElement(it) }
}