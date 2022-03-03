package me.jakejmattson.discordkt.arguments

public open class AutocompleteArg<Input, Output>(override val name: String,
                                                 override val description: String,
                                                 override val type: PrimitiveArgument<Input, Output>,
                                                 internal val autocomplete: suspend AutocompleteData.() -> List<Input>) : WrappedArgument<Input, Output, Input, Output>