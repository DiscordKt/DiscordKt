package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.stringify
import java.awt.Color

/**
 * Accepts a color in hexadecimal format. The '#' symbol is optional.
 */
public open class HexColorArg(
    override val name: String = "Color",
    override val description: String = internalLocale.hexColorArgDescription
) : StringArgument<Color> {
    /**
     * Accepts a color in hexadecimal format. The '#' symbol is optional.
     */
    public companion object : HexColorArg()

    override suspend fun transform(input: String, context: DiscordContext): Either<String, Color> = either {
        ensure(input.length in 6..7) {
            internalLocale.invalidFormat
        }

        val trimmedInput = input.takeLast(6).uppercase()
        val isValidHex = trimmedInput.all { it in '0'..'9' || it in 'A'..'F' }

        ensure(isValidHex) {
            internalLocale.invalidFormat
        }

        Color(trimmedInput.toInt(16))
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        listOf(stringify(Color((0..255).random(), (0..255).random(), (0..255).random())))
}