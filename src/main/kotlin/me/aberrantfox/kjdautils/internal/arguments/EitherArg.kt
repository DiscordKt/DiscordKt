package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

sealed class Either<out E, out V> {
    data class Left<out E>(val left: E) : Either<E, Nothing>()
    data class Right<out V>(val right: V) : Either<Nothing, V>()
}

// Either accept the left argument or the right argument type. Left is tried first.
class EitherArg(val left: ArgumentType<*>, val right: ArgumentType<*>, name: String = ""): ArgumentType<Either<*, *>> {
    override val name = if (name.isNotBlank()) name else "${left.name} | ${right.name}"
    override val examples: ArrayList<String> = ArrayList(left.examples + right.examples)
    override val consumptionType = ConsumptionType.Single
    init {
        if (left.consumptionType != ConsumptionType.Single || right.consumptionType != ConsumptionType.Single)
            throw IllegalArgumentException()
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Either<*, *>> {
        val leftResult = left.convert(arg, args, event)
        val rightResult = right.convert(arg, args, event)

        return when {
            leftResult is ArgumentResult.Success -> ArgumentResult.Success(Either.Left(leftResult.result))
            rightResult is ArgumentResult.Success -> ArgumentResult.Success(Either.Left(rightResult.result))
            else -> rightResult as ArgumentResult.Error<Either<*, *>>
        }
    }
}

infix fun ArgumentType<*>.or(right: ArgumentType<*>) = EitherArg(this, right)