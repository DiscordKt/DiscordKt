package me.jakejmattson.discordkt.api.dsl.configuration

import me.jakejmattson.discordkt.internal.utils.diService

/**
 * @suppress Used in sample
 */
class InjectionConfiguration {
    fun inject(vararg injectionObjects: Any) = injectionObjects.forEach { diService.inject(it) }
}