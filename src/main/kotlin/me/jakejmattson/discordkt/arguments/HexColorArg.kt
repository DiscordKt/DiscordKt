package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import java.awt.Color

/**
 * Accepts a color in hexadecimal format. The '#' symbol is optional.
 */
public open class HexColorArg(override val name: String = "Hex Color",
                              override val description: String = internalLocale.hexColorArgDescription) : Argument<Color> {
    /**
     * Accepts a color in hexadecimal format. The '#' symbol is optional.
     */
    public companion object : HexColorArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Color> {
        if (arg.length !in 6..7) return Error(internalLocale.invalidFormat)

        val trimmedInput = arg.takeLast(6).uppercase()
        val isValidHex = trimmedInput.all { it in '0'..'9' || it in 'A'..'F' }

        if (!isValidHex)
            return Error(internalLocale.invalidFormat)

        val color = Color(trimmedInput.toInt(16))

        return Success(color)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf(formatData(Color((0..255).random(), (0..255).random(), (0..255).random())))
    override fun formatData(data: Color): String = with(data) {
        String.format("#%02X%02X%02X", red, green, blue)
    }
}