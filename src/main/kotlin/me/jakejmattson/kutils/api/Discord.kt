package me.jakejmattson.kutils.api

import com.google.gson.Gson
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.configuration.KConfiguration
import me.jakejmattson.kutils.internal.event.EventRegister
import me.jakejmattson.kutils.internal.utils.diService
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import kotlin.reflect.KClass

data class KUtilsProperties(val kutilsVersion: String, val kotlinVersion: String, val jdaVersion: String, val repository: String)

private val propFile = KUtilsProperties::class.java.getResource("/kutils-properties.json").readText()

abstract class Discord {
    @Deprecated("To be removed")
    abstract val jda: JDA
    abstract val configuration: KConfiguration
    val properties = Gson().fromJson(propFile, KUtilsProperties::class.java)

    internal abstract fun addEventListener(register: EventRegister)

    @Deprecated("Use classes as parameters", ReplaceWith("discord.getInjectionObjects(T::class)"))
    inline fun <reified T> getInjectionObject() = diService.getElement(T::class.java) as T

    inline fun <reified A : Any> getInjectionObjects(obj: KClass<A>) = getInjectionObject<A>()

    inline fun <reified A : Any, reified B : Any>
        getInjectionObjects(obj1: KClass<A>, obj2: KClass<B>)
        = Args2(getInjectionObject<A>(), getInjectionObject<B>())

    inline fun <reified A : Any, reified B : Any, reified C : Any>
        getInjectionObjects(obj1: KClass<A>, obj2: KClass<B>, obj3: KClass<C>)
        = Args3(getInjectionObject<A>(), getInjectionObject<B>(), getInjectionObject<C>())

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any>
        getInjectionObjects(obj1: KClass<A>, obj2: KClass<B>, obj3: KClass<C>, obj4: KClass<D>)
        = Args4(getInjectionObject<A>(), getInjectionObject<B>(), getInjectionObject<C>(), getInjectionObject<D>())

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any>
        getInjectionObjects(obj1: KClass<A>, obj2: KClass<B>, obj3: KClass<C>, obj4: KClass<D>, obj5: KClass<E>)
        = Args5(getInjectionObject<A>(), getInjectionObject<B>(), getInjectionObject<C>(), getInjectionObject<D>(), getInjectionObject<E>())
}

internal fun buildDiscordClient(token: String, configuration: KConfiguration): Discord {
    val jda = JDABuilder(token).build()
    jda.awaitReady()
    return object : Discord() {
        override val jda: JDA = jda
        override val configuration: KConfiguration = configuration

        override fun addEventListener(register: EventRegister) {
            jda.addEventListener(object : EventListener {
                override fun onEvent(event: GenericEvent) = register.onEvent(event)
            })
        }
    }
}