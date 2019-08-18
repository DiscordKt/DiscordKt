package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

private const val deprecationMessage = "Use property access. EOL 0.11.x"

class EmbedDSLHandle {
    var title : String? = null
    var description : String? = null
    var color : Color? = null
    var thumbnail : String? = null
    var fields : MutableList<FieldStore> = mutableListOf()

    operator fun invoke(args: EmbedDSLHandle.() -> Unit) {}

    @Deprecated(deprecationMessage, ReplaceWith("apply { this.title = t }"), DeprecationLevel.WARNING)
    fun setTitle(t: String?) : EmbedDSLHandle = apply { this.title = t }

    @Deprecated(deprecationMessage, ReplaceWith("apply { this.description = d }"), DeprecationLevel.WARNING)
    fun setDescription(d: String?) : EmbedDSLHandle = apply { this.description = d }

    @Deprecated(deprecationMessage, ReplaceWith("apply { this.color = c }"), DeprecationLevel.WARNING)
    fun setColor(c: Color) : EmbedDSLHandle = apply { this.color = c }

    @Deprecated(deprecationMessage, ReplaceWith("apply { this.thumbnail = t }"), DeprecationLevel.WARNING)
    fun setThumbnail(t: String?) : EmbedDSLHandle = apply { this.thumbnail = t }

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
    fun addBlankField(inline: Boolean) = fields.add(FieldStore("", "", inline))

    fun build() =
        EmbedBuilder().apply {
            setTitle(title)
            setDescription(description)
            setColor(color)
            setThumbnail(thumbnail)
            this@EmbedDSLHandle.fields.forEach { addField(it.name, it.value, it.inline) }
        }.build()
    }

data class FieldStore(var name: String? = "", var value: String? = "", var inline: Boolean = false)

fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}
