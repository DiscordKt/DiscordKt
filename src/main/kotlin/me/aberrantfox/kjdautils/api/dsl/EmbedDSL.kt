package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color

class EmbedDSLHandle : EmbedBuilder() {
    operator fun invoke(args: EmbedDSLHandle.() -> Unit) {}

    fun title(title: String?) = this.setTitle(title)

    fun description(descr: String?) = this.setDescription(descr)

    fun color(color: Color) = this.setColor(color)

    fun field(construct: FieldStore.() -> Unit) {
        val field = FieldStore()
        field.construct()
        addField(field.name, field.value, false)
    }

    fun ifield(construct: FieldStore.() -> Unit) {
        val field = FieldStore()
        field.construct()
        addField(field.name, field.value, true)
    }
}

data class FieldStore(var name: String? = "", var value: String? = "", var inline: Boolean = true)

fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}