@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.embed

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.temporal.TemporalAccessor

private typealias EmbedField = MessageEmbed.Field

class EmbedDSLHandle {
    companion object {
        lateinit var successColor: Color
        lateinit var failureColor: Color
        lateinit var infoColor: Color
    }

    /** @suppress Redundant doc */
    val successColor: Color
        get() = Companion.successColor

    /** @suppress Redundant doc */
    val failureColor: Color
        get() = Companion.failureColor

    /** @suppress Redundant doc */
    val infoColor: Color
        get() = Companion.infoColor

    private val mutableFields = mutableListOf<EmbedField>()
    private var author: MessageEmbed.AuthorInfo? = null
    private var titleBundle: TitleBuilder? = null
    private var footer: MessageEmbed.Footer? = null

    /**
     * Alternative simple value instead of the [title] builder.
     */
    var simpleTitle: String? = null
        set(value) {
            titleBundle = TitleBuilder(value)
        }

    /** @suppress Redundant doc */
    var description: String? = null

    /** @suppress Redundant doc */
    var color: Color? = null

    /** @suppress Redundant doc */
    var thumbnail: String? = null

    /** @suppress Redundant doc */
    var image: String? = null

    /** @suppress Redundant doc */
    var timeStamp: TemporalAccessor? = null

    /** @suppress Redundant doc */
    fun title(construct: TitleBuilder.() -> Unit) {
        val titleBuilder = TitleBuilder()
        titleBuilder.construct()
        titleBundle = titleBuilder
    }

    /** @suppress Redundant doc */
    fun author(construct: AuthorBuilder.() -> Unit) {
        val authorBuilder = AuthorBuilder()
        authorBuilder.construct()
        author = authorBuilder.build()
    }

    /** @suppress */
    fun field(construct: FieldBuilder.() -> Unit) {
        val fieldBuilder = FieldBuilder()
        fieldBuilder.construct()
        mutableFields.add(fieldBuilder.build())
    }

    /** @suppress */
    fun footer(construct: FooterBuilder.() -> Unit) {
        val footerBuilder = FooterBuilder()
        footerBuilder.construct()
        footer = footerBuilder.build()
    }

    /** @suppress Redundant doc */
    fun addField(field: EmbedField) = mutableFields.add(field)

    /** @suppress Redundant doc */
    fun addField(name: String?, value: String?, inline: Boolean = false) = addField(EmbedField(name, value, inline))

    /** @suppress Redundant doc */
    fun addInlineField(name: String?, value: String?) = addField(EmbedField(name, value, true))

    /** @suppress Redundant doc */
    fun addBlankField(inline: Boolean) = addField(EmbedField("", "", inline))

    /**
     * Build the embed and apply the DSL configuration.
     */
    fun build() = EmbedBuilder().apply {
        fields.addAll(mutableFields)
        setTitle(titleBundle?.text, titleBundle?.url)
        setDescription(description)
        setColor(color)
        setThumbnail(thumbnail)
        setImage(image)
        setTimestamp(timeStamp)
        setAuthor(author?.name, author?.url, author?.iconUrl)
        setFooter(footer?.text, footer?.iconUrl)
    }.build()

    /** @suppress */
    data class TitleBuilder(var text: String? = "", var url: String? = null)

    /** @suppress */
    data class AuthorBuilder(var name: String? = "", var url: String? = null, var iconUrl: String? = null) {
        fun build() = MessageEmbed.AuthorInfo(name, url, iconUrl, null)
    }

    /** @suppress */
    data class FieldBuilder(var name: String? = "", var value: String? = "", var inline: Boolean = false) {
        fun build() = EmbedField(name, value, inline)
    }

    /** @suppress */
    data class FooterBuilder(var text: String? = "", var iconUrl: String? = null) {
        fun build() = MessageEmbed.Footer(text, iconUrl, null)
    }
}

/**
 * Construct an embed using the DSL.
 */
fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}

/**
 * Convert a Discord embed entity back into the builder format.
 */
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