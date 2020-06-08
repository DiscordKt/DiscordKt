package arguments

import me.jakejmattson.kutils.api.arguments.HexColorArg
import utilities.ArgumentTestFactory
import java.awt.Color

class HexColorArgTest : ArgumentTestFactory {
    override val argumentType = HexColorArg

    override val validArgs = listOf(
        "#000000" to Color(0x000000),
        "000000" to Color(0x000000),
        "#FFFFFF" to Color(0xFFFFFF),
        "FFFFFF" to Color(0xFFFFFF),
        "#00bFfF" to Color(0x00BFFF)
    )

    override val invalidArgs = listOf("black", "0", "#gggggg", "FFFFFFFF")
}