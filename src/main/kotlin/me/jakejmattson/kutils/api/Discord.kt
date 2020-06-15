@file:Suppress("unused")

package me.jakejmattson.kutils.api

import com.google.gson.Gson
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.configuration.KConfiguration
import me.jakejmattson.kutils.internal.event.EventRegister
import me.jakejmattson.kutils.internal.utils.diService
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import kotlin.reflect.KClass

data class KUtilsProperties(val repository: String,
                            val kutilsVersion: String,
                            val kotlinVersion: String,
                            val jdaVersion: String)

private val propFile = KUtilsProperties::class.java.getResource("/kutils-properties.json").readText()

abstract class Discord {
    @Deprecated("To be removed")
    abstract val jda: JDA
    abstract val configuration: KConfiguration
    val properties = Gson().fromJson(propFile, KUtilsProperties::class.java)!!

    internal abstract fun addEventListener(register: EventRegister)

    @Deprecated("Use classes as parameters", ReplaceWith("discord.getInjectionObjects(T::class)"))
    inline fun <reified T> getInjectionObject() = diService.getElement<T>()

    inline fun <reified A : Any> getInjectionObjects(a: KClass<A>) = diService.getElement<A>()

    inline fun <reified A : Any, reified B : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>) =
        Args2(getInjectionObjects(a), getInjectionObjects(b))

    inline fun <reified A : Any, reified B : Any, reified C : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>) =
        Args3(getInjectionObjects(a), getInjectionObjects(b), getInjectionObjects(c))

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>) =
        Args4(getInjectionObjects(a), getInjectionObjects(b), getInjectionObjects(c), getInjectionObjects(d))

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any>
        getInjectionObjects(a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>, e: KClass<E>) =
        Args5(getInjectionObjects(a), getInjectionObjects(b), getInjectionObjects(c), getInjectionObjects(d), getInjectionObjects(e))
}

internal fun buildDiscordClient(token: String, configuration: KConfiguration) =
    object : Discord() {
        override val jda: JDA = JDABuilder.createDefault(token)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .build()
            .also { it.awaitReady() }

        override val configuration: KConfiguration = configuration

        override fun addEventListener(register: EventRegister) {
            jda.addEventListener(object : EventListener {
                override fun onEvent(event: GenericEvent) = register.onEvent(event)
            })
        }
    }