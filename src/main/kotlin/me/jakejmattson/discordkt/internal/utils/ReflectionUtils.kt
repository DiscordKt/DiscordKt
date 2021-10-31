package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.CommandSet
import me.jakejmattson.discordkt.dsl.Listeners
import me.jakejmattson.discordkt.dsl.Precondition
import me.jakejmattson.discordkt.dsl.diService
import org.reflections.Reflections
import org.reflections.scanners.Scanners.MethodsReturn
import org.reflections.scanners.Scanners.SubTypes
import org.reflections.scanners.Scanners.TypesAnnotated
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass

internal interface BuilderRegister {
    fun register(discord: Discord)
}

internal lateinit var Reflection: ReflectionUtils

internal class ReflectionUtils(path: String) {
    private val reflections = Reflections(path, SubTypes, TypesAnnotated, MethodsReturn)

    fun registerFunctions(discord: Discord) {
        register<CommandSet>(discord)
        register<Listeners>(discord)
        register<Precondition>(discord)
    }

    private inline fun <reified T : BuilderRegister> register(discord: Discord) = reflections
        .get(MethodsReturn.with(T::class.java).`as`(Method::class.java))
        .forEach {
            diService.invokeMethod<T>(it).register(discord)
        }

    inline fun <reified T : Annotation> detectClassesWith(): Set<Class<*>> = reflections.get(SubTypes.of<T>(TypesAnnotated.with(T::class.java)).asClass<T>())
    inline fun <reified T> detectSubtypesOf(): Set<Class<*>> = reflections.get(TypesAnnotated.with(T::class.java).asClass<T>())
}

internal val Class<*>.simplerName
    get() = toString().substringAfterLast('.').substringBefore('$')

@PublishedApi
internal val KClass<*>.simplerName: String
    get() = java.simplerName

internal val Method.signature
    get() = "${name}(${parameterTypes.joinToString { it.simplerName }})"

internal val Constructor<*>.signature
    get() = "${name}(${parameterTypes.joinToString { it.simplerName }})"