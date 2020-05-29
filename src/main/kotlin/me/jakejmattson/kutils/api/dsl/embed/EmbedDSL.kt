@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.embed

import me.aberrantfox.kutils.api.dsl.configuration.ColorConfiguration
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.temporal.TemporalAccessor

private typealias EmbedField = MessageEmbed.Field

class EmbedDSLHandle {
    companion object {
        private val defaultColors = ColorConfiguration()

        var successColor: Color = defaultColors.successColor
        var failureColor: Color = defaultColors.failureColor
        var infoColor: Color = defaultColors.infoColor
    }

    val successColor: Color
        get() = Companion.successColor

    val failureColor: Color
        get() = Companion.failureColor

    val infoColor: Color
        get() = Companion.infoColor

    private val mutableFields: MutableList<EmbedField> = mutableListOf()
    private var author: MessageEmbed.AuthorInfo? = null
    private var footer: MessageEmbed.Footer? = null

    var title: String? = null
    var description: String? = null
    var color: Color? = null
    var thumbnail: String? = null
    var image: String? = null
    var timeStamp: TemporalAccessor? = null

    fun author(construct: AuthorBuilder.() -> Unit) {
        val authorBuilder = AuthorBuilder()
        authorBuilder.construct()
        author = authorBuilder.build()
    }

    fun field(construct: FieldBuilder.() -> Unit) {
        val fieldBuilder = FieldBuilder()
        fieldBuilder.construct()
        mutableFields.add(fieldBuilder.build())
    }

    fun footer(construct: FooterBuilder.() -> Unit) {
        val footerBuilder = FooterBuilder()
        footerBuilder.construct()
        footer = footerBuilder.build()
    }

    fun addField(field: EmbedField) = mutableFields.add(field)
    fun addField(name: String?, value: String?, inline: Boolean = false) = addField(EmbedField(name, value, inline))
    fun addInlineField(name: String?, value: String?) = addField(EmbedField(name, value, true))
    fun addBlankField(inline: Boolean) = addField(EmbedField("", "", inline))

    fun build(): MessageEmbed {
        val embedBuilder = EmbedBuilder().apply {
            fields.addAll(mutableFields)
            setTitle(title)
            setDescription(description)
            setColor(color)
            setThumbnail(thumbnail)
            setImage(image)
            setTimestamp(timeStamp)
            setAuthor(author?.name, author?.url, author?.iconUrl)
            setFooter(footer?.text, footer?.iconUrl)
        }

        return embedBuilder.build()
    }
}

data class FieldBuilder(var name: String? = "", var value: String? = "", var inline: Boolean = false) {
    fun build() = EmbedField(name, value, inline)
}

data class AuthorBuilder(var name: String? = "", var url: String? = null, var iconUrl: String? = null) {
    fun build() = MessageEmbed.AuthorInfo(name, url, iconUrl, null)
}

data class FooterBuilder(var text: String? = "", var iconUrl: String? = null) {
    fun build() = MessageEmbed.Footer(text, iconUrl, null)
}

fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}

fun MessageEmbed.toEmbedBuilder() =
    EmbedBuilder().apply {
        setTitle(title)
        setDescription(description)
        setFooter(footer?.text, footer?.iconUrl)
        setThumbnail(thumbnail?.url)
        setTimestamp(timestamp)
        setImage(image?.url)
        setColor(colorRaw)
        setAuthor(author?.name)
        fields.addAll(this@toEmbedBuilder.fields)
    }