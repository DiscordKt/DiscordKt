package me.jakejmattson.discordkt.internal.services

import me.jakejmattson.discordkt.internal.utils.InternalLogger
import me.jakejmattson.discordkt.internal.utils.signature
import me.jakejmattson.discordkt.internal.utils.simplerName
import java.lang.reflect.Method
import kotlin.reflect.KClass

internal data class FailureBundle(val clazz: Class<*>, val parameters: List<Class<*>>)

@PublishedApi
internal class InjectionService {
    private val elementMap = HashMap<Class<*>, Any>()

    fun inject(element: Any) {
        elementMap[element::class.java] = element
    }

    operator fun <T : Any> get(clazz: KClass<T>) = elementMap[clazz.java] as? T
        ?: throw IllegalArgumentException("Could not find ${clazz.simplerName}")

    internal inline fun <reified T> invokeMethod(method: Method): T {
        val objects = determineArguments(method.parameterTypes, method.signature)
        return method.invoke(null, *objects) as T
    }

    private fun <T> invokeConstructor(clazz: Class<T>): T {
        val constructor = clazz.constructors.first()
        val objects = determineArguments(constructor.parameterTypes, constructor.signature)
        return constructor.newInstance(*objects) as T
    }

    fun buildAllRecursively(services: Set<Class<*>>, last: Int = -1) {
        val failed = services.mapNotNull {
            try {
                val result = invokeConstructor(it)
                inject(result)
                null
            } catch (e: IllegalStateException) {
                it
            }
        }.toSet().takeIf { it.isNotEmpty() } ?: return

        val sortedFailures = failed
            .map { it to it.constructors.first() }
            .sortedBy { (_, constructor) -> constructor.parameterCount }
            .map { (clazz, constructor) -> FailureBundle(clazz, constructor.parameterTypes.toList()) }

        if (failed.size == last)
            InternalLogger.fatalError(generateBadInjectionReport(sortedFailures))

        buildAllRecursively(failed, failed.size)
    }

    private fun determineArguments(parameters: Array<out Class<*>>, signature: String) = if (parameters.isEmpty()) emptyArray() else
        parameters.map { arg ->
            elementMap.entries.find { arg.isAssignableFrom(it.key) }?.value
                ?: throw IllegalStateException("Couldn't inject '${arg.simplerName}' into $signature")
        }.toTypedArray()

    private fun generateBadInjectionReport(failedInjections: List<FailureBundle>) = buildString {
        appendLine("Dependency injection error!")
        appendLine("Unable to build the following:")

        val failedDependencyList = failedInjections.joinToString("\n") { (clazz, types) ->
            "${clazz.simplerName}(${types.joinToString { it.simplerName }})"
        }

        appendLine(failedDependencyList)

        val failedClasses = failedInjections.map { it.clazz }

        val (failed, missing) = failedInjections
            .flatMap { (_, parameters) ->
                parameters.mapNotNull { clazz ->
                    clazz.takeIf { parameter -> parameter !in elementMap }
                }
            }
            .distinct()
            .partition { it in failedClasses }

        if (missing.isNotEmpty()) {
            appendLine()
            appendLine("Missing base dependencies:")
            appendLine(missing.joinToString("\n") { it.simplerName })
        }

        if (failed.isNotEmpty()) {
            val failPaths = failed
                .mapNotNull { getCyclicList(it) }
                .distinctBy { it.toSet() }
                .joinToString("\n") { classes ->
                    classes.joinToString(" -> ") { it.simplerName }
                }

            appendLine()
            appendLine("Infinite loop detected:")
            append(failPaths)
        }
    }
}

private fun getCyclicList(target: Class<*>) = mutableListOf(target).takeIf { pathList ->
    target.children.any { hasPath(it, pathList, target) }
}

private fun hasPath(root: Class<*>, path: MutableList<Class<*>>, target: Class<*>): Boolean {
    path.add(root)

    if (root == target || root.children.any { hasPath(it, path, target) })
        return true

    path.removeAt(path.lastIndex)
    return false
}

private val Class<*>.children
    get() = constructors.firstOrNull()?.parameterTypes ?: emptyArray()