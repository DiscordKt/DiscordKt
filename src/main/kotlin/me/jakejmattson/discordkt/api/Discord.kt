@file:Suppress("unused")

package me.jakejmattson.discordkt.api

import com.gitlab.kordlib.core.Kord
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import me.jakejmattson.discordkt.api.dsl.Command
import me.jakejmattson.discordkt.api.dsl.BotConfiguration
import me.jakejmattson.discordkt.internal.utils.diService
import kotlin.reflect.KClass

/**
 * @param library The current DiscordKt version.
 * @param kotlin The version of Kotlin used by DiscordKt.
 * @param kord The version of Kord used by DiscordKt.
 */
@Serializable
data class Versions(val library: String, val kotlin: String, val kord: String)

/**
 * @property api A Kord instance used to access the Discord API.
 * @property configuration All of the current configuration details for this bot.
 * @property commands All registered commands.
 * @property versions Properties for the core library.
 */
abstract class Discord {
    abstract val api: Kord
    abstract val configuration: BotConfiguration
    abstract val commands: MutableList<Command>
    val versions = Json.decodeFromString<Versions>(this::class.java.getResource("/library-properties.json").readText())

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any> getInjectionObjects(a: KClass<A>) = diService[a]

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>) = Args2(diService[a], diService[b])

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any, reified C : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>) = Args3(diService[a], diService[b], diService[c])

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>) =
        Args4(diService[a], diService[b], diService[c], diService[d])

    /** Fetch an object from the DI pool by its type */
    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>, e: KClass<E>) =
        Args5(diService[a], diService[b], diService[c], diService[d], diService[e])
}

internal fun buildDiscordClient(api: Kord, configuration: BotConfiguration) =
    object : Discord() {
        override val api = api
        override val configuration = configuration
        override val commands = mutableListOf<Command>()
    }