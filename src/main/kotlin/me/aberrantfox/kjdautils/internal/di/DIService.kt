package me.aberrantfox.kjdautils.internal.di

import java.lang.reflect.Method

class DIService {
    private val elementMap = HashMap<Class<*>, Any>()

    fun addElement(element: Any) = elementMap.put(element::class.java, element)

    fun invokeReturningMethod(method: Method): Any {
        val arguments: Array<out Class<*>> = method.parameterTypes

        if (arguments.isEmpty())
            return method.invoke(null)

        val objects = arguments.map { arg ->
            elementMap.entries
                    .find { arg.isAssignableFrom(it.key) }
                    ?.value
                    ?: throw IllegalStateException("Couldn't inject $arg from registered objects")
        }.toTypedArray()

        return method.invoke(null, *objects)
    }
}