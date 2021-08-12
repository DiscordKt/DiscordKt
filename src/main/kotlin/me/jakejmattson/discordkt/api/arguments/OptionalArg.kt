package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.locale.inject

/**
 * An optional argument with a default value.
 */
class OptionalArg<G>(override val name: String,
                     private val type: Argument<*>,
                     private val default: suspend CommandEvent<*>.() -> G) : Argument<G> {
    override val description = internalLocale.optionalArgDescription.inject(type.name)

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<G> {
        val conversion = type.convert(arg, args, event)

        return if (conversion is Success)
            conversion as ArgumentResult<G>
        else
            Success(default.invoke(event), 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = type.generateExamples(event)
}