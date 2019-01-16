package me.aberrantfox.kjdautils.internal.di

import com.google.gson.GsonBuilder
import me.aberrantfox.kjdautils.api.annotation.Data
import java.io.File
import java.lang.reflect.Method

class DIService {
    private val elementMap = HashMap<Class<*>, Any>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        addElement(PersistenceService(this))
    }

    fun addElement(element: Any) = elementMap.put(element::class.java, element)

    fun invokeReturningMethod(method: Method): Any {
        val arguments: Array<out Class<*>> = method.parameterTypes

        if (arguments.isEmpty())
            return method.invoke(null)

        val objects = determineArguments(arguments)
        return method.invoke(null, *objects)
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

        if(failed.size == 0) {
            return
        }

        if(failed.size == last) {
            throw IllegalStateException("Attempted to reflectively build up dependencies, however an infinite loop was detected." +
                    " Are all dependencies properly marked and available?")
        }

        invokeDestructiveList(failed, failed.size)
    }

    fun collectDataObjects(dataObjs: Set<Class<*>>) {
        dataObjs.forEach {
            val path =  it.getAnnotation(Data::class.java).path
            val file = File(path)
            val parent = file.parentFile

            if(parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            if(file.exists()) {
                val contents = file.readText()
                elementMap[it] = gson.fromJson(contents, it)
            } else {
                val obj = it.getConstructor().newInstance()
                file.writeText(gson.toJson(obj, it))
                elementMap[it] = obj
            }
        }
    }

    fun saveObject(obj: Any) {
        val clazz = obj::class.java

        if( !(elementMap.containsKey(clazz)) ) return

        if(clazz.annotations.none { it::class.java == Data::class.java }) return

        val annotation = clazz.getAnnotation(Data::class.java)
        val file = File(annotation.path)

        file.writeText(gson.toJson(obj))
        elementMap[clazz] = obj
    }

    private fun determineArguments(arguments: Array<out Class<*>>) =
            arguments.map { arg ->
                elementMap.entries
                        .find { arg.isAssignableFrom(it.key) }
                        ?.value
                        ?: throw IllegalStateException("Couldn't inject $arg from registered objects")
            }.toTypedArray()
}

class PersistenceService(val diService: DIService) {
    fun save(obj: Any) = diService.saveObject(obj)
}