package me.jakejmattson.discordkt.api.dsl

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import java.io.File

/**
 * A class that represents some data in a JSON file. This will be registered into the dependency pool automatically.
 *
 * @property path The file path on the system where the data is located.
 * @property killIfGenerated Whether the program should exit if this file was not present.
 * @property file The file obtained from the provided path.
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

    /**
     * Save the modified data object back into the injection pool and write to file.
     */
    fun save() {
        writeToFile()
        diService.inject(this)
    }
}