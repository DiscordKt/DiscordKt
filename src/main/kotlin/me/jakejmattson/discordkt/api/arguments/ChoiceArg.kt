package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.internal.utils.InternalLogger

/**
 * Accepts a choice from the provided list.
 *
 * @param choices The available choices. Can be any type, but associated by toString value.
 */
open class ChoiceArg<T>(override val name: String, vararg choices: T) : ArgumentType<T> {
    private val enumerations = choices.associateBy { it.toString().lowercase() }
    private val options = enumerations.keys
    override val description = "A list to choose from"

    init {
        if (choices.size != options.size)
            InternalLogger.error("ChoiceArg elements must be unique.")
    }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T> {
        val selection = enumerations[arg.lowercase()]
            ?: return Error("Invalid selection")

        return Success(selection)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = options.toList()
}