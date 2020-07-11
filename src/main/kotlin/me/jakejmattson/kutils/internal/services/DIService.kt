package me.jakejmattson.kutils.internal.services

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.internal.utils.*
import java.lang.reflect.Method
import kotlin.system.exitProcess

internal data class FailureBundle(val clazz: Class<*>, val parameters: List<Class<*>>)

@PublishedApi
internal class DIService {
    val elementMap = HashMap<Class<*>, Any>()

    fun inject(element: Any) {
        elementMap[element::class.java] = element
    }

    @PublishedApi
    internal inline fun <reified T> getElement() = elementMap[T::class.java] as T

    internal inline fun <reified T> invokeMethod(method: Method): T {
        val objects = determineArguments(method.parameterTypes)
        val result = method.invoke(null, *objects) as? T

        if (result == null) {
            displayReturnError(method)
            exitProcess(-1)
        }

        return result
    }

    internal fun <T> invokeConstructor(clazz: Class<T>): T {
        val constructor = clazz.constructors.first()
        val objects = determineArguments(constructor.parameterTypes)
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
            InternalLogger.error(generateBadInjectionReport(sortedFailures)).also { exitProcess(-1) }

        buildAllRecursively(failed, failed.size)
    }

    private fun determineArguments(parameters: Array<out Class<*>>) = if (parameters.isEmpty()) emptyArray() else
        parameters.map { arg ->
            elementMap.entries.find { arg.isAssignableFrom(it.key) }?.value
                ?: throw IllegalStateException("Couldn't inject of type '$arg' from registered objects.")
        }.toTypedArray()

    private fun displayReturnError(method: Method) {
        val signatureBase = with(method) {
            "$name(${parameterTypes.joinToString(",") { it.name }}) = "
        }

        val annotation = method.annotations.firstOrNull()
        val annotationName = "@${annotation?.annotationClass?.simpleName}"

        val (invocationInfo, expectedReturn) = when (annotation) {
            is CommandSet -> "$annotationName(\"${annotation.category}\") fun $signatureBase" to "commands { ... }"
            else -> annotationName to "<Unknown>"
        }

        val currentSignature = invocationInfo + method.returnType
        val suggestedSignature = invocationInfo + expectedReturn

        InternalLogger.error("An annotated function didn't return the correct type.\n" +
            "Signature: $currentSignature\n" +
            "Suggested: $suggestedSignature")
    }

    private fun generateBadInjectionReport(failedInjections: List<FailureBundle>) = buildString {
        appendln("Dependency injection error!")
        appendln("Unable to build the following:")

        val failedDependencyList = failedInjections.joinToString("\n") { (clazz, types) ->
            "${clazz.simplerName}(${types.joinToString { it.simplerName }})"
        }

        appendln(failedDependencyList)

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
            appendln()
            appendln("Missing base dependencies:")
            appendln(missing.joinToString("\n") { it.simplerName })
        }

        if (failed.isNotEmpty()) {
            val failPaths = failed
                .mapNotNull { getCyclicList(it) }
                .distinctBy { it.toSet() }
                .joinToString("\n") { it.joinToString(" -> ") { it.simplerName } }

            appendln()
            appendln("Infinite loop detected:")
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