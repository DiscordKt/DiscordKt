package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * Accepts multiple arguments of the given type. Returns a list.
 *
 * @param base The [Argument] that you expect to be used to create the list.
 */
public class MultipleArg<Input, Output>(override val base: Argument<Input, Output>,
                                       override val name: String = base.name,
                                       description: String = "") : WrappedArgument<Input, Output, List<Input>, List<Output>> {
    override val description: String = description.ifBlank { internalLocale.multipleArgDescription.inject(base.name) }

    override suspend fun parse(args: MutableList<String>, discord: Discord): List<Input>? {
        val totalResult = mutableListOf<Input>()
        val remainingArgs = args.toMutableList()

        complete@ while (remainingArgs.isNotEmpty()) {
            val conversion = base.parse(remainingArgs, discord)

            if (conversion != null) {
                totalResult.add(conversion)
            } else {
                if (totalResult.isEmpty())
                    return null

                break@complete
            }
        }

        return totalResult
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        base.generateExamples(context).chunked(2).map { it.joinToString(" ") }
}