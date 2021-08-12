@file:Suppress("unused")

package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.locale.inject

/**
 * An [Either] type
 *
 * @property data The stored value
 */
data class Left<out L>(val data: L) : Either<L, Nothing>()

/**
 * An [Either] type
 *
 * @property data The stored value
 */
data class Right<out R>(val data: R) : Either<Nothing, R>()

/**
 * Represent 2 possible types in a single object.
 */
sealed class Either<out L, out R> {
    /**
     * Map the actual internal value to either the left or right predicate.
     *
     * @param left The value map if the left element is present.
     * @param right The value map if the right element is present.
     */
    suspend fun <T> map(left: suspend (L) -> T, right: suspend (R) -> T) =
        when (this) {
            is Left -> left.invoke(data)
            is Right -> right.invoke(data)
        }
}

/**
 * Accept either the left argument or the right [Argument].
 *
 * @param left The first [Argument] to attempt to convert the data to.
 * @param right The second [Argument] to attempt to convert the data to.
 */
class EitherArg<L, R>(val left: Argument<L>, val right: Argument<R>, name: String = "", description: String = "") : Argument<Either<L, R>> {
    override val name = name.ifBlank { "${left.name} | ${right.name}" }
    override val description = description.ifBlank { internalLocale.eitherArgDescription.inject(left.name, right.name) }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Either<L, R>> {
        val leftResult = left.convert(arg, args, event)
        val rightResult = right.convert(arg, args, event)

        return when {
            leftResult is Success -> Success(Left(leftResult.result), leftResult.consumed)
            rightResult is Success -> Success(Right(rightResult.result), rightResult.consumed)
            else -> Error("Matched neither expected args")
        }
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> {
        val leftExample = left.generateExamples(event).takeIf { it.isNotEmpty() }?.random() ?: "<Example>"
        val rightExample = right.generateExamples(event).takeIf { it.isNotEmpty() }?.random() ?: "<Example>"

        return listOf("$leftExample | $rightExample")
    }
}

/**
 * Syntactic sugar for creating an EitherArg from the two given types.
 */
infix fun <L, R> Argument<L>.or(right: Argument<R>) = EitherArg(this, right)