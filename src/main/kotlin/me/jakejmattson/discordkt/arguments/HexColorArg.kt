package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.internal.utils.stringify
import java.awt.Color

/**
 * Accepts a color in hexadecimal format. The '#' symbol is optional.
 */
public open class HexColorArg(override val name: String = "Color",
                              override val description: String = internalLocale.hexColorArgDescription) : StringArgument<Color> {
    /**
     * Accepts a color in hexadecimal format. The '#' symbol is optional.
     */
    public companion object : HexColorArg()

    override suspend fun transform(input: String, context: DiscordContext): Result<Color> {
        if (input.length !in 6..7) return Error(internalLocale.invalidFormat)

        val trimmedInput = input.takeLast(6).uppercase()
        val isValidHex = trimmedInput.all { it in '0'..'9' || it in 'A'..'F' }

        if (!isValidHex)
            return Error(internalLocale.invalidFormat)

        return Success(Color(trimmedInput.toInt(16)))
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(stringify(Color((0..255).random(), (0..255).random(), (0..255).random())))
}