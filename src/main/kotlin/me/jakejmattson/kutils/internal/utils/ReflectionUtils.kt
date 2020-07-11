package me.jakejmattson.kutils.internal.utils

import com.google.common.eventbus.Subscribe
import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.dsl.command.CommandsContainer
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import java.lang.reflect.Method

internal object ReflectionUtils {
    fun detectCommands(path: String): CommandsContainer {
        val commandSets = detectMethodsWith<CommandSet>(path)
            .map { diService.invokeMethod<CommandsContainer>(it) to it.getAnnotation<CommandSet>().category }

        if (commandSets.isEmpty()) {
            InternalLogger.startup("0 CommandSets -> 0 Commands")
            return CommandsContainer()
        }

        val allCommands = commandSets
            .flatMap { (container, cmdSetCategory) ->
                container.commands.apply {
                    forEach { it.category = it.category.ifBlank { cmdSetCategory } }
                }
            }.toMutableList()

        InternalLogger.startup(commandSets.size.pluralize("CommandSet") + " -> " + allCommands.size.pluralize("Command"))

        return CommandsContainer(allCommands)
    }

    fun detectListeners(path: String) = detectMethodsWith<Subscribe>(path)
        .map { it.declaringClass }
        .distinct()
        .map { diService.invokeConstructor(it) }

    inline fun <reified T : Annotation> detectClassesWith(path: String): Set<Class<*>> = Reflections(path).getTypesAnnotatedWith(T::class.java)
    inline fun <reified T> detectSubtypesOf(path: String): Set<Class<out T>> = Reflections(path).getSubTypesOf(T::class.java)
    inline fun <reified T : Annotation> detectMethodsWith(path: String): Set<Method> = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(T::class.java)
}

internal inline fun <reified T : Annotation> Method.getAnnotation() = getAnnotation(T::class.java)

internal val Class<*>.simplerName
    get() = simpleName.substringAfterLast(".")