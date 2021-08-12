package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.commands.CommandSet
import me.jakejmattson.discordkt.api.dsl.*
import org.reflections.Reflections
import org.reflections.scanners.MethodParameterScanner
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass

internal interface BuilderRegister {
    fun register(discord: Discord)
}

internal object ReflectionUtils {
    fun registerFunctions(path: String, discord: Discord) {
        register<CommandSet>(path, discord)
        register<Listeners>(path, discord)
        register<Precondition>(path, discord)
    }

    private inline fun <reified T : BuilderRegister> register(path: String, discord: Discord) = detectMethodsReturning<T>(path).forEach {
        diService.invokeMethod<T>(it).register(discord)
    }

    inline fun <reified T : Annotation> detectClassesWith(path: String): Set<Class<*>> = Reflections(path).getTypesAnnotatedWith(T::class.java)
    inline fun <reified T> detectSubtypesOf(path: String): Set<Class<out T>> = Reflections(path).getSubTypesOf(T::class.java)
    private inline fun <reified T> detectMethodsReturning(path: String): MutableSet<Method> = Reflections(path, MethodParameterScanner()).getMethodsReturn(T::class.java)
}

internal val Class<*>.simplerName
    get() = toString().substringAfterLast('.').substringBefore('$')

@PublishedApi
internal val KClass<*>.simplerName
    get() = java.simplerName

internal val Method.signature
    get() = "${name}(${parameterTypes.joinToString { it.simplerName }})"

internal val Constructor<*>.signature
    get() = "${name}(${parameterTypes.joinToString { it.simplerName }})"