package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import java.io.File

open class FileArg(override val name: String = "File") : ArgumentType<File>() {
    companion object : FileArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<File> {
        val attachments = event.message.attachments

        if (attachments.isEmpty())
            return ArgumentResult.Error("$name argument requires a message attachment.")

        val file = attachments.first().downloadToFile().get()

        return ArgumentResult.Success(file, 0)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("File")
}