package me.jakejmattson.kutils.api.services

import com.google.gson.GsonBuilder
import me.jakejmattson.kutils.api.annotations.*
import me.jakejmattson.kutils.internal.utils.InternalLogger
import java.io.File
import java.lang.reflect.Method
import kotlin.system.exitProcess

class DIService {
    private val elementMap = HashMap<Class<*>, Any>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        addElement(PersistenceService(this))
    }

    fun addElement(element: Any) = elementMap.put(element::class.java, element)

    fun getElement(serviceClass: Class<*>) = elementMap[serviceClass]

    internal inline fun <reified T> invokeReturningMethod(method: Method): T {
        val arguments: Array<out Class<*>> = method.parameterTypes
        val objects = if (arguments.isNotEmpty()) determineArguments(arguments) else emptyArray()
        val result = method.invoke(null, *objects) as? T

        if (result == null) {
            badInjectionExit(method)
            exitProcess(-1)
        }

        return result
    }

    fun invokeConstructor(clazz: Class<*>): Any {
        val constructor = clazz.constructors.first()
        val arguments = constructor.parameterTypes

        if (arguments.isEmpty())
            return constructor.newInstance()

        val objects = determineArguments(arguments)

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

        if (failed.size == 0) {
            return
        }

        val sortedFailedDependencies = failed
            .sortedBy { it.constructors.first().parameterCount }
            .map { Pair(it.simpleName, it.constructors.first().parameterCount) }

        val failedDependencies = sortedFailedDependencies
            .groupBy({ it.second }) { it.first }
            .entries.joinToString("\n") { "Dependencies with ${it.key} parameters: ${it.value.joinToString(", ")}" }

        check(failed.size != last) {
            "Attempted to reflectively build up dependencies, however an infinite loop was detected." +
                " Are all dependencies properly marked and available? You can see a list of failed dependencies" +
                "here sorted by how many parameters their constructors have. Double check ones with the lowest params first" +
                "that is where the error is.:\n$failedDependencies"
        }

        invokeDestructiveList(failed, failed.size)
    }

    fun collectDataObjects(dataObjs: Set<Class<*>>): ArrayList<String> {
        val dataRequiringFillRestart = ArrayList<String>()

        dataObjs.forEach {
            val annotation = it.getAnnotation(Data::class.java)
            val path = annotation.path
            val file = File(path)
            val parent = file.parentFile

            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            val alreadyGenerated = file.exists()

            if (file.exists()) {
                val contents = file.readText()
                elementMap[it] = gson.fromJson(contents, it)
            } else {
                val obj = it.getConstructor().newInstance()
                file.writeText(gson.toJson(obj, it))
                elementMap[it] = obj
            }

            if (annotation.killIfGenerated && !alreadyGenerated) {
                dataRequiringFillRestart.add(file.absolutePath)
            }
        }

        return dataRequiringFillRestart
    }

    fun saveObject(obj: Any) {
        val clazz = obj::class.java

        require((elementMap.containsKey(clazz))) { "You may only pass @Data annotated objects to PersistenceService#save" }

        val annotation = clazz.getAnnotation(Data::class.java) ?: return

        val file = File(annotation.path)

        file.writeText(gson.toJson(obj))
        elementMap[clazz] = obj
    }

    private fun determineArguments(arguments: Array<out Class<*>>) =
        arguments.map { arg ->
            val instance = elementMap.entries
                .find { arg.isAssignableFrom(it.key) }
                ?.value
                ?: if (arg == ScriptEngineService::class.java)
                    throw IllegalStateException("ScriptEngineService must be enabled in startBot() before using.")
                else
                    throw IllegalStateException("Couldn't inject of type '$arg' from registered objects.")

            instance
        }.toTypedArray()

    private fun badInjectionExit(method: Method) {
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

class PersistenceService(private val diService: DIService) {
    fun save(obj: Any) = diService.saveObject(obj)
}