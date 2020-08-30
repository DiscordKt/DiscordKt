package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Register
import me.jakejmattson.discordkt.api.dsl.*
import org.reflections8.Reflections
import org.reflections8.scanners.MethodAnnotationsScanner
import java.lang.reflect.Method

internal object ReflectionUtils {
    fun fireRegisteredFunctions(path: String, discord: Discord) {
        detectMethodsWith<Register>(path).forEach {
            when (it.returnType) {
                CommandSet::class.java -> diService.invokeMethod<CommandSet>(it).registerCommands(discord)
                Listeners::class.java -> diService.invokeMethod<Listeners>(it).registerListeners(discord)
                else -> diService.invokeMethod(it)
            }
        }
    }

    inline fun <reified T : Annotation> detectClassesWith(path: String): Set<Class<*>> = Reflections(path).getTypesAnnotatedWith(T::class.java)
    inline fun <reified T : Annotation> detectMethodsWith(path: String): Set<Method> = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(T::class.java)
    inline fun <reified T> detectSubtypesOf(path: String): Set<Class<out T>> = Reflections(path).getSubTypesOf(T::class.java)
}

internal val Class<*>.simplerName
    get() = simpleName.substringAfterLast(".")