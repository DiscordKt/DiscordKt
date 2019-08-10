package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

class EmbedDSLHandle {
    var title : String? = null
    var description : String? = null
    var color : Color? = null
    var thumbnail : String? = null
    var fields : MutableList<FieldStore> = mutableListOf()

    operator fun invoke(args: EmbedDSLHandle.() -> Unit) {}

    @Deprecated("use title")
    fun setTitle(t: String?) : EmbedDSLHandle = apply { this.title = t }

    @Deprecated("use description")
    fun setDescription(d: String?) : EmbedDSLHandle = apply { this.description = d }

    @Deprecated("use color")
    fun setColor(c: Color) : EmbedDSLHandle = apply { this.color = c }

    @Deprecated("use thumbnail")
    fun setThumbnail(t: String?) : EmbedDSLHandle = apply { this.thumbnail = t }

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

    fun addField(name: String?, value: String?, inline: Boolean = false) = fields.add(FieldStore(name, value, inline))
    fun addInlineField(name: String?, value: String?) = fields.add(FieldStore(name, value, true))

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
