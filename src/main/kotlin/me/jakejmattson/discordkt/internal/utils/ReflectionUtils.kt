package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.annotations.CommandSet
import me.jakejmattson.discordkt.api.dsl.command.Command
import me.jakejmattson.discordkt.api.extensions.stdlib.pluralize
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import java.lang.reflect.Method

internal object ReflectionUtils {
    fun detectCommands(path: String): MutableList<Command> {
        val commandSets = detectMethodsWith<CommandSet>(path)
            .map { diService.invokeMethod<MutableList<Command>>(it) to it.getAnnotation<CommandSet>().category }

        if (commandSets.isEmpty()) {
            InternalLogger.startup("0 CommandSets -> 0 Commands")
            return mutableListOf()
        }

        val commands = commandSets
            .flatMap { (commands, cmdSetCategory) ->
                commands.apply {
                    forEach { it.category = it.category.ifBlank { cmdSetCategory } }
                }
            }.toMutableList()

        InternalLogger.startup(commandSets.size.pluralize("CommandSet") + " -> " + commands.size.pluralize("Command"))

        return commands
    }

    private inline fun <reified T : Annotation> Method.getAnnotation() = getAnnotation(T::class.java)
    inline fun <reified T : Annotation> detectClassesWith(path: String): Set<Class<*>> = Reflections(path).getTypesAnnotatedWith(T::class.java)
    inline fun <reified T : Annotation> detectMethodsWith(path: String): Set<Method> = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(T::class.java)
    inline fun <reified T> detectSubtypesOf(path: String): Set<Class<out T>> = Reflections(path).getSubTypesOf(T::class.java)
}

internal val Class<*>.simplerName
    get() = simpleName.substringAfterLast(".")