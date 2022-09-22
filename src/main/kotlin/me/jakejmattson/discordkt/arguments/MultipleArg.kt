package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Args1
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.internal.command.transformArgs
import me.jakejmattson.discordkt.locale.inject

/**
 * Accepts multiple arguments of the given type. Returns a list.
 *
 * @param type The [Argument] that you expect to be used to create the list.
 */
public class MultipleArg<Input, Output>(override val type: Argument<Input, Output>,
                                        override val name: String = type.name,
                                        description: String = "") : WrappedArgument<Input, Output, List<Input>, List<Output>> {
    override val description: String = description.ifBlank { internalLocale.multipleArgDescription.inject(type.name) }

    override suspend fun transform(input: List<Input>, context: DiscordContext): Result<List<Output>> {
        val transformation = input.map {
            transformArgs(listOf(type to it), context)
        }

        return if (transformation.all { it is Success })
            Success(transformation.map { (((it as Success).result) as Args1<*>).first as Output })
        else {
            transformation.first { it is Error } as Error<List<Output>>
        }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        type.generateExamples(context).chunked(2).map { it.joinToString(" ") }
}