package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.internal.utils.InternalLogger

/**
 * Accepts a choice from the provided list.
 *
 * @param choices The available choices. Can be any type, but associated by toString value.
 */
public open class ChoiceArg<T>(override val name: String,
                               override val description: String = internalLocale.choiceArgDescription,
                               vararg choices: T) : StringArgument<T> {
    private val enumerations = choices.associateBy { it.toString().lowercase() }

    /**
     * The available choices. Can be any type, but associated by toString value.
     */
    public val choices: List<T> = choices.toList()

    init {
        if (choices.size != choices.distinct().size)
            InternalLogger.error("ChoiceArg elements must be unique.")
    }

    override suspend fun transform(input: String, context: DiscordContext): Result<T> {
        val selection = enumerations[input.lowercase()]
            ?: return Error("Invalid selection")

        return Success(selection)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = choices.map { it.toString() }
}