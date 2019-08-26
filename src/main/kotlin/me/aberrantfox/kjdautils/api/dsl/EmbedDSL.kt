package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import java.awt.Color
import java.time.temporal.TemporalAccessor

private typealias EmbedField = MessageEmbed.Field

class EmbedDSLHandle {
    var mutableFields : MutableList<EmbedField> = mutableListOf()

    var title : String? = null
    var description : String? = null
    var color : Color? = null
    var thumbnail : String? = null
    var image: String? = null
    var author: User? = null
    var timeStamp: TemporalAccessor? = null

    fun field(construct: FieldBuilder.() -> Unit) {
        val fieldBuilder = FieldBuilder()
        fieldBuilder.construct()
        mutableFields.add(fieldBuilder.build())
    }

    fun ifield(construct: FieldBuilder.() -> Unit) {
        val fieldBuilder = FieldBuilder()
        fieldBuilder.construct()
        mutableFields.add(fieldBuilder.build())
    }

    fun addField(field: EmbedField) = mutableFields.add(field)
    fun addField(name: String?, value: String?, inline: Boolean = false) = addField(EmbedField(name, value, inline))
    fun addInlineField(name: String?, value: String?) = addField(EmbedField(name, value, true))
    fun addBlankField(inline: Boolean) = addField(EmbedField("", "", inline))

    fun build() =
        EmbedBuilder().apply {
            this.fields.addAll(mutableFields)
            setTitle(title)
            setDescription(description)
            setColor(color)
            setThumbnail(thumbnail)
            setImage(image)
            setTimestamp(timeStamp)

            if (author != null)
                setAuthor(author!!.name, null, author!!.effectiveAvatarUrl)
        }.build()
}

data class FieldBuilder(var name: String? = "", var value: String? = "", var inline: Boolean = false) {
    fun build() = EmbedField(name, value, inline)
}

fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}
