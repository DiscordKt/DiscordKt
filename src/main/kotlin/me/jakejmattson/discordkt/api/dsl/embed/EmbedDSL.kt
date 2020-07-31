@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.embed

import me.jakejmattson.discordkt.api.annotations.BuilderDSL
import me.jakejmattson.discordkt.api.dsl.configuration.ColorConfiguration
import me.jakejmattson.discordkt.internal.utils.EmbedField
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.temporal.TemporalAccessor

/**
 * Type-safe build for creating Discord embeds.
 */
class EmbedDSL {
    companion object {
        private val defaults = ColorConfiguration()

        internal var successColor: Color = defaults.successColor
        internal var failureColor: Color = defaults.failureColor
        internal var infoColor: Color = defaults.infoColor
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
    private var titleBundle: Title? = null
    private var footer: MessageEmbed.Footer? = null

    /**
     * Alternative simple value instead of the title builder function.
     */
    var simpleTitle: String? = null
        set(value) {
            titleBundle = Title(value)
        }

    /** @suppress External doc */
    var description: String? = null

    /** @suppress External doc */
    var color: Color? = null

    /** @suppress External doc */
    var thumbnail: String? = null

    /** @suppress External doc */
    var image: String? = null

    /** @suppress External doc */
    var timeStamp: TemporalAccessor? = null

    /** @suppress External doc */
    fun title(construct: Title.() -> Unit) {
        val titleBuilder = Title()
        titleBuilder.construct()
        titleBundle = titleBuilder
    }

    /** @suppress External doc */
    fun author(construct: Author.() -> Unit) {
        val authorBuilder = Author()
        authorBuilder.construct()
        author = authorBuilder.build()
    }

    /** @suppress External doc */
    fun field(construct: Field.() -> Unit) {
        val fieldBuilder = Field()
        fieldBuilder.construct()
        mutableFields.add(fieldBuilder.build())
    }

    /**
     * @sample Footer
     */
    fun footer(construct: Footer.() -> Unit) {
        val footerBuilder = Footer()
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

    internal fun build() = EmbedBuilder().apply {
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

    /** @suppress DSL backing */
    data class Title(var text: String? = "", var url: String? = null)

    /** @suppress DSL backing */
    data class Author(var name: String? = "", var url: String? = null, var iconUrl: String? = null) {
        internal fun build() = MessageEmbed.AuthorInfo(name, url, iconUrl, null)
    }

    /** @suppress DSL backing */
    data class Field(var name: String? = "", var value: String? = "", var inline: Boolean = false) {
        internal fun build() = EmbedField(name, value, inline)
    }

    /** @suppress DSL backing */
    data class Footer(var text: String? = "", var iconUrl: String? = null) {
        internal fun build() = MessageEmbed.Footer(text, iconUrl, null)
    }
}

/** @suppress DSL Builder */
@BuilderDSL
fun embed(construct: EmbedDSL.() -> Unit): MessageEmbed {
    val handle = EmbedDSL()
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