package me.aberrantfox.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent

internal data class Left<out L>(val data: L) : Either<L, Nothing>()
internal data class Right<out R>(val data: R) : Either<Nothing, R>()

sealed class Either<out L, out R> {
    fun <T> getData(left: (L) -> T, right: (R) -> T) =
        when (this) {
            is Left -> left.invoke(data)
            is Right -> right.invoke(data)
        }
}

// Either accept the left argument or the right argument type. Left is tried first.
class EitherArg<L, R>(val left: ArgumentType<L>, val right: ArgumentType<R>, name: String = "") : ArgumentType<Either<L, R>>() {
    override val name = if (name.isNotBlank()) name else "${left.name} | ${right.name}"

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Either<L, R>> {
        val leftResult = left.convert(arg, args, event)
        val rightResult = right.convert(arg, args, event)

        return when {
            leftResult is ArgumentResult.Success -> ArgumentResult.Success(Left(leftResult.result), leftResult.consumed)
            rightResult is ArgumentResult.Success -> ArgumentResult.Success(Right(rightResult.result), rightResult.consumed)
            else -> ArgumentResult.Error("Could not match input with either expected argument.")
        }
    }

    override fun generateExamples(event: CommandEvent<*>): List<String> {
        val leftExample = left.generateExamples(event).takeIf { it.isNotEmpty() }?.random() ?: "<Example>"
        val rightExample = right.generateExamples(event).takeIf { it.isNotEmpty() }?.random() ?: "<Example>"

        return listOf("$leftExample | $rightExample")
    }
}

infix fun <L, R> ArgumentType<L>.or(right: ArgumentType<R>) = EitherArg(this, right)