package me.jakejmattson.kutils.api.dsl.data

import com.google.gson.*
import me.jakejmattson.kutils.internal.utils.diService
import java.io.File

/**
 * A class that represents some data in a JSON file.
 */
abstract class Data(val path: String, val killIfGenerated: Boolean = true) {
    val file = File(path)
    private val gson = GsonBuilder()
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>) = false
            override fun shouldSkipField(f: FieldAttributes?) = f?.declaringClass == Data::class.java
        })
        .setPrettyPrinting()
        .create()

    internal fun readFromFile() = gson.fromJson(file.readText(), this::class.java)
    internal fun writeToFile() {
        val parent = file.parentFile

        if (parent != null && !parent.exists())
            parent.mkdirs()

        File(path).writeText(gson.toJson(this))
    }

    fun save() {
        writeToFile()
        diService.inject(this)
    }
}