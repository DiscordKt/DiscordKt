package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.*
import org.reflections.Reflections
import org.reflections.scanners.*
import java.lang.reflect.Method
import kotlin.reflect.KClass

internal object ReflectionUtils {
    fun registerFunctions(path: String, discord: Discord) {
        detectMethodsReturning<CommandSet>(path).forEach {
            diService.invokeMethod<CommandSet>(it).registerCommands(discord)
        }

        detectMethodsReturning<Listeners>(path).forEach {
            diService.invokeMethod<Listeners>(it).registerListeners(discord)
        }
    }

    inline fun <reified T : Annotation> detectClassesWith(path: String): Set<Class<*>> = Reflections(path).getTypesAnnotatedWith(T::class.java)
    inline fun <reified T : Annotation> detectMethodsWith(path: String): Set<Method> = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(T::class.java)
    inline fun <reified T> detectSubtypesOf(path: String): Set<Class<out T>> = Reflections(path).getSubTypesOf(T::class.java)
    private inline fun <reified T> detectMethodsReturning(path: String): MutableSet<Method> = Reflections(path, MethodParameterScanner()).getMethodsReturn(T::class.java)
}

internal val Class<*>.simplerName
    get() = simpleName.substringAfterLast(".").substringBefore("$")

internal val KClass<*>.simplerName
    get() = java.simplerName