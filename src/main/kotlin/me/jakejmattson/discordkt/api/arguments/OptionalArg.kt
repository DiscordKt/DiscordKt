package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent

class OptionalArg<G>(override val name: String, val type: ArgumentType<*>, val default: suspend CommandEvent<*>.() -> G) : ArgumentType<G> {
    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<G> {
        val conversion = type.convert(arg, args, event)

        return if (conversion is Success)
            conversion as ArgumentResult<G>
        else
            Success(default.invoke(event), 0)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> {
        return listOf("")
    }
}