package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.interaction.AutoCompleteInteraction

/**
 * The data provided to process autocomplete interactions.
 *
 * @param interaction The Discord interaction.
 * @param input The user input thus far.
 */
public data class AutocompleteData(public val interaction: AutoCompleteInteraction, public val input: String)

/**
 * Provides autocomplete options to slash input.
 */
public open class AutocompleteArg<Input, Output>(override val name: String,
                                                 override val description: String,
                                                 override val type: PrimitiveArgument<Input, Output>,
                                                 internal val autocomplete: suspend AutocompleteData.() -> List<Input>) : WrappedArgument<Input, Output, Input, Output>