package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.internal.utils.InternalLogger

/**
 * Accepts a choice from the provided list.
 *
 * @param choices The available choices. Can be any type, but associated by toString value.
 */
open class ChoiceArg<T>(override val name: String, vararg choices: T) : ArgumentType<T>() {
    private val enumerations = choices.associateBy { it.toString().toLowerCase() }
    private val options = enumerations.keys

    init {
        if (choices.size != options.size)
            InternalLogger.error("ChoiceArg elements must be unique.")
    }

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>): ArgumentResult<T> {
        val selection = enumerations[arg.toLowerCase()]
            ?: return Error("Invalid selection")

        return Success(selection)
    }

    override fun generateExamples(event: GlobalCommandEvent<*>) = options.toList()
}