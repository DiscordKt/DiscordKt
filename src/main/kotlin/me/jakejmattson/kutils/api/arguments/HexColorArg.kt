package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import java.awt.Color

/**
 * Accepts a color in hexadecimal format. The '#' symbol is optional.
 */
open class HexColorArg(override val name: String = "Hex Color") : ArgumentType<Color>() {
    /**
     * Accepts a color in hexadecimal format. The '#' symbol is optional.
     */
    companion object : HexColorArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Color> {
        if (arg.length !in 6..7) return Error("Couldn't parse $name from $arg. Hex colors are 6 digits long.")

        val trimmedInput = arg.takeLast(6).toUpperCase()
        val isValidHex = trimmedInput.all { it in '0'..'9' || it in 'A'..'F' }

        if (!isValidHex)
            return Error("Couldn't parse $name from $arg. Hex colors range from 0 to F")

        val color = Color(trimmedInput.toInt(16))

        return Success(color)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("#000000", "ffffff")
}
