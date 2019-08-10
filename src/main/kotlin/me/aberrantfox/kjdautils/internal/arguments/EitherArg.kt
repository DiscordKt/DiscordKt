package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

sealed class Either<out E, out V> {
    data class Left<out E>(val left: E) : Either<E, Nothing>()
    data class Right<out V>(val right: V) : Either<Nothing, V>()
}

// Either accept the left argument or the right argument type. Left is tried first.
class EitherArg(val left: ArgumentType, val right: ArgumentType, name: String = "") : ArgumentType {
    override val name = if (name.isNotBlank()) name else "${left.name} | ${right.name}"
    override val examples: ArrayList<String> = ArrayList(left.examples + right.examples)
    override val consumptionType = ConsumptionType.Single
    init {
        if (left.consumptionType != ConsumptionType.Single || right.consumptionType != ConsumptionType.Single)
            throw IllegalArgumentException()
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val leftResult = left.convert(arg, args, event)
        return if (leftResult is ArgumentResult.Single)
            ArgumentResult.Single(Either.Left(leftResult.result))
        else {
            val rightResult = right.convert(arg, args, event)
            return if (rightResult is ArgumentResult.Single)
                ArgumentResult.Single(Either.Right(rightResult.result))
            else
                rightResult
        }
    }
}

infix fun ArgumentType.or(right: ArgumentType) = EitherArg(this, right)