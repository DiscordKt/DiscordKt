package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * An optional argument with a default value.
 */
public class OptionalArg<G>(override val name: String,
                            internal val type: Argument<*>,
                            private val default: suspend CommandEvent<*>.() -> G) : Argument<G> {
    override val description: String = internalLocale.optionalArgDescription.inject(type.name)

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<G> {
        val conversion = type.convert(arg, args, event)

        return if (conversion is Success)
            conversion as ArgumentResult<G>
        else
            Success(default.invoke(event), 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = type.generateExamples(event)
}