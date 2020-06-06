package me.jakejmattson.kutils.internal.utils

import com.google.common.eventbus.Subscribe
import me.jakejmattson.kutils.api.annotations.*
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.preconditions.*
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

internal object ReflectionUtils {
    fun detectCommands(path: String): CommandsContainer {
        val commandSets = detectMethods<CommandSet>(path)
            .map { diService.invokeReturningMethod<CommandsContainer>(it) to it.getAnnotation(CommandSet::class.java).category }

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

    fun detectListeners(path: String) = detectMethods<Subscribe>(path)
        .map { it.declaringClass }
        .distinct()
        .map { diService.invokeConstructor(it) }

    fun detectPreconditions(path: String) = detectMethods<Precondition>(path)
        .map {
            val annotation = it.getAnnotation(Precondition::class.java)
            val condition = diService.invokeReturningMethod<(CommandEvent<*>) -> PreconditionResult>(it)

            PreconditionData(condition, annotation.priority)
        }

    fun detectData(path: String) = detectClasses<Data>(path)
    fun detectServices(path: String) = detectClasses<Service>(path)

    private inline fun <reified T : Annotation> detectClasses(path: String) = Reflections(path).getTypesAnnotatedWith(T::class.java)
    private inline fun <reified T : Annotation> detectMethods(path: String) = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(T::class.java)
}