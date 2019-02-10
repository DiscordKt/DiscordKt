package me.aberrantfox.kjdautils.internal.di

import com.google.gson.GsonBuilder
import me.aberrantfox.kjdautils.api.annotation.Data
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.reflect.Method

class DIService {
    private val elementMap = HashMap<Class<*>, Any>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        addElement(PersistenceService(this))
    }

    fun addElement(element: Any) = elementMap.put(element::class.java, element)

    private val nullReturnException = IllegalArgumentException(
            "A commands container, conversation, or precondition function hasn't returned properly.\n" +
                    "Check that your '@CommandSet', '@Precondition', or '@Convo' functions properly return the 'commands { ... }', 'precondition { ... }', or 'conversation { ... }' calls.\n" +
                    "e.g. Make sure that the '=' is used in '@CommandSet fun commandSet(...) = commands { ... }'\n" +
                    " or '@Precondition fun preconditionFunc() = precondition { ... }'\n" +
                    " or '@Convo fun testConversation(...) = conversation { ... }'"
    )

    fun invokeReturningMethod(method: Method): Any {
        val arguments: Array<out Class<*>> = method.parameterTypes

        if (arguments.isEmpty())
            return method.invoke(null) ?: throw nullReturnException

        val objects = determineArguments(arguments)
        return method.invoke(null, *objects) ?: throw nullReturnException
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

        if( !(elementMap.containsKey(clazz)) )
            throw IllegalArgumentException("You may only pass @Data annotated objects to PersistenceService#save")

        val annotation = clazz.getAnnotation(Data::class.java) ?: return

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

class PersistenceService(private val diService: DIService) {
    fun save(obj: Any) = diService.saveObject(obj)
}