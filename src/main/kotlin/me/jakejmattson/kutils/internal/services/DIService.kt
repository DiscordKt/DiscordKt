package me.jakejmattson.kutils.internal.services

import com.google.gson.GsonBuilder
import me.jakejmattson.kutils.api.annotations.*
import me.jakejmattson.kutils.api.services.ScriptEngineService
import me.jakejmattson.kutils.internal.utils.*
import java.io.File
import java.lang.reflect.Method
import kotlin.system.exitProcess

@PublishedApi
internal class DIService {
    val elementMap = HashMap<Class<*>, Any>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun addElement(element: Any) = elementMap.put(element::class.java, element)

    @PublishedApi
    internal inline fun <reified T> getElement() = elementMap[T::class.java] as T

    internal inline fun <reified T> invokeReturningMethod(method: Method): T {
        val objects = determineArguments(method.parameterTypes)
        val result = method.invoke(null, *objects) as? T

        if (result == null) {
            displayReturnError(method)
            exitProcess(-1)
        }

        return result
    }

    internal fun invokeConstructor(clazz: Class<*>): Any {
        val constructor = clazz.constructors.first()
        val objects = determineArguments(constructor.parameterTypes)
        return constructor.newInstance(*objects)
    }

    fun invokeDestructiveList(services: Set<Class<*>>, last: Int = -1) {
        val failed = hashSetOf<Class<*>>()

        services.forEach {
            try {
                val result = invokeConstructor(it)
                addElement(result)
            } catch (e: IllegalStateException) {
                failed.add(it)
            }
        }

        println(elementMap)
        println(failed)

        if (failed.isEmpty())
            return

        val sortedFailures = failed
            .map { it.simplerName to it.constructors.first() }
            .sortedBy { (_, constructor) -> constructor.parameterCount }
            .map { (name, constructor) -> name to constructor.parameterTypes }
            .joinToString("\n") { (name, types) ->
                "$name(${types.joinToString { it.simplerName }})"
            }

        if(failed.size != last) {
            InternalLogger.error("Unable to build the following dependencies:\n$sortedFailures")
            exitProcess(-1)
        }

        invokeDestructiveList(failed, failed.size)
    }

    fun collectDataObjects(dataObjs: Set<Class<*>>) = dataObjs.mapNotNull {
        val annotation = it.getAnnotation<Data>()
        val file = File(annotation.path)
        val parent = file.parentFile

        if (parent != null && !parent.exists())
            parent.mkdirs()

        val alreadyGenerated = file.exists()

        if (file.exists()) {
            val contents = file.readText()
            elementMap[it] = gson.fromJson(contents, it)
        } else {
            val obj = it.getConstructor().newInstance()
            file.writeText(gson.toJson(obj, it))
            elementMap[it] = obj
        }

        if (annotation.killIfGenerated && !alreadyGenerated) file.absolutePath else null
    }

    fun saveObject(obj: Any) {
        val clazz = obj::class.java
        val annotation = clazz.getAnnotation<Data>()
            ?: throw IllegalArgumentException("PersistenceService#save parameters must be annotated with @Data")

        File(annotation.path).writeText(gson.toJson(obj))
        elementMap[clazz] = obj
    }

    private fun determineArguments(parameters: Array<out Class<*>>) = if (parameters.isEmpty()) emptyArray() else
        parameters.map { arg ->
            elementMap.entries.find { arg.isAssignableFrom(it.key) }?.value
                ?: throw IllegalStateException(
                    when (arg) {
                        ScriptEngineService::class.java -> "ScriptEngineService must be enabled in startBot() before using."
                        else -> "Couldn't inject of type '$arg' from registered objects."
                    }
                )
        }.toTypedArray()

    private fun displayReturnError(method: Method) {
        val signatureBase = with(method) {
            "$name(${parameterTypes.joinToString(",") { it.name }}) = "
        }

        val annotation = method.annotations.firstOrNull()
        val annotationName = "@${annotation?.annotationClass?.simpleName}"

        val (invocationInfo, expectedReturn) = when (annotation) {
            is CommandSet -> "$annotationName(\"${annotation.category}\") fun $signatureBase" to "commands { ... }"
            is Precondition -> "$annotationName(${annotation.priority}) fun $signatureBase" to "precondition { ... }"
            else -> annotationName to "<Unknown>"
        }

        val currentSignature = invocationInfo + method.returnType
        val suggestedSignature = invocationInfo + expectedReturn

        InternalLogger.error("An annotated function didn't return the correct type.\n" +
            "Signature: $currentSignature\n" +
            "Suggested: $suggestedSignature")
    }
}