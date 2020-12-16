package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent

/**
 * An optional argument with a default value.
 */
class OptionalArg<G>(override val name: String, private val type: ArgumentType<*>, private val default: suspend CommandEvent<*>.() -> G) : ArgumentType<G> {
    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<G> {
        val conversion = type.convert(arg, args, event)

        return if (conversion is Success)
            conversion as ArgumentResult<G>
        else
            Success(default.invoke(event), 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = type.generateExamples(event)
}