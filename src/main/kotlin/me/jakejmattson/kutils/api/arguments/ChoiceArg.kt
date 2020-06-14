package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class ChoiceArg<T>(override val name: String, vararg choices: T) : ArgumentType<String>() {
    private val enumerations = choices.associateBy { it.toString().toLowerCase() }
    private val options = enumerations.keys

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        val selection = enumerations[arg.toLowerCase()] as? String
            ?: return ArgumentResult.Error("Invalid choice for $name. Available choices: ${options.joinToString()}")

        return ArgumentResult.Success(selection)
    }

    override fun generateExamples(event: CommandEvent<*>) = options.toList()
}