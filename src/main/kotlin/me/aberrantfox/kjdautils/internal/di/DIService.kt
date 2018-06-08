package me.aberrantfox.kjdautils.internal.di

import java.lang.reflect.Method

class DIService {
    private val elementMap = HashMap<Class<*>, Any>()

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

    private fun determineArguments(arguments: Array<out Class<*>>) =
            arguments.map { arg ->
                elementMap.entries
                        .find { arg.isAssignableFrom(it.key) }
                        ?.value
                        ?: throw IllegalStateException("Couldn't inject $arg from registered objects")
            }.toTypedArray()
}