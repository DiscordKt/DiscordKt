package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*
import java.awt.Color

open class HexColorArg(override val name : String = "Hex Color"): ArgumentType<Color>() {
    companion object : HexColorArg()

    override val consumptionType = ConsumptionType.Single
    override val examples = mutableListOf("#000000", "ffffff")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Color> {
        if (arg.length !in 6..7) return ArgumentResult.Error("Invalid format. Hex colors are 6 digits in length.")

        val trimmedInput = arg.takeLast(6).toUpperCase()
        val isValidHex = trimmedInput.all { it in '0'..'9' || it in 'A'..'F' }

        if (!isValidHex)
            return ArgumentResult.Error("Invalid format. Hexadecimal numbers range from 0 to F")

        val int = trimmedInput.toInt(16)

        return ArgumentResult.Success(Color(int))
    }
}
