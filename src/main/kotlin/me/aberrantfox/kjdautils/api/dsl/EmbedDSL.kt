package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

class EmbedDSLHandle {
    private var title : String? = null
    private var description : String? = null
    private var color : Color? = null
    private var thumbnail : String? = null
    private var fields : MutableList<FieldStore> = mutableListOf()

    operator fun invoke(args: EmbedDSLHandle.() -> Unit) {}

    fun title(t: String?) = { title = t }
    fun description(d: String?) = { description = d }
    fun color(c: Color) = { color = c }
    fun thumbnail(t: String?) = { thumbnail = t }

    @Deprecated("use title")
    fun setTitle(t: String?) : EmbedDSLHandle = apply { title(t) }

    @Deprecated("use description")
    fun setDescription(d: String?) : EmbedDSLHandle = apply { description(d) }

    @Deprecated("use color")
    fun setColor(c: Color) : EmbedDSLHandle = apply { color(c) }

    @Deprecated("use thumbnail")
    fun setThumbnail(t: String?) : EmbedDSLHandle = apply { thumbnail(t) }

    fun addBlankField(inl: Boolean) = field {
        inline = inl
    }

    fun field(construct: FieldStore.() -> Unit) {
        val field = FieldStore()
        field.construct()
        fields.add(field)
    }

    fun ifield(construct: FieldStore.() -> Unit) {
        val field = FieldStore()
        field.construct()
        fields.add(field)
    }

    fun build() : MessageEmbed {
        val b = EmbedBuilder()
        title.let { b.setTitle(it) }
        description.let { b.setDescription(it) }
        color.let { b.setColor(it) }
        thumbnail.let { b.setThumbnail(it) }

        fields.forEach { b.addField(it.name, it.value, it.inline) }

        return b.build()
    }
}

data class FieldStore(var name: String? = "", var value: String? = "", var inline: Boolean = false)

fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}
