package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class HexColorArg(override val name : String = "Hex Color"): ArgumentType<Int>() {
    companion object : HexColorArg()

    override val examples = arrayListOf("#000000", "FFFF00", "#3498db", "db3434")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {
        val error = "Invalid color argument."

        if (arg.length !in 6..7) return ArgumentResult.Error(error)

        val int = try { arg.takeLast(6).toInt(16) } catch (e: NumberFormatException) { return ArgumentResult.Error(error) }

        return if (int >= 0) ArgumentResult.Success(int) else ArgumentResult.Error(error)
    }
}
