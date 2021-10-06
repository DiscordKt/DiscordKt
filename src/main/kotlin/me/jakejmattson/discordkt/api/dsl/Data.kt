package me.jakejmattson.discordkt.api.dsl

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import java.io.File

/**
 * A serializable class that represents some data.
 */
@Serializable @Polymorphic
public abstract class Data {
    @PublishedApi
    @Transient
    internal lateinit var file: File

    /**
     * Write the data object content to its file as JSON.
     */
    public fun save() {
        val parent = file.parentFile

        if (parent != null && !parent.exists())
            parent.mkdirs()

        file.writeText(serializer.encodeToString(this))
    }

    public companion object {
        @PublishedApi
        @Transient
        internal var module: SerializersModule = SerializersModule {}

        @PublishedApi
        @Transient
        internal val serializer: Json
            get() = Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
                serializersModule = module
            }

        @PublishedApi
        internal inline fun <reified T : Data> subclass() {
            module += SerializersModule {
                polymorphic(Data::class) {
                    subclass(T::class)
                }
            }
        }
    }
}