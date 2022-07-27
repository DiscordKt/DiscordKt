package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext

/**
 * An [Argument] that wraps around another argument.
 */
public interface WrappedArgument<Input, Output, Input2, Output2> : Argument<Input2, Output2> {
    /**
     * The [Argument] that is wrapped.
     */
    public val type: Argument<Input, Output>

    /**
     * Get the innermost/raw type inside this argument.
     */
    public val innerType: Argument<Input, Output>
        get() {
            var inner: Argument<*, *> = type

            while (inner is WrappedArgument<*, *, *, *>)
                inner = inner.type

            return inner as Argument<Input, Output>
        }

    override suspend fun parse(args: MutableList<String>, discord: Discord): Input2? = type.parse(args, discord) as Input2?
    override suspend fun generateExamples(context: DiscordContext): List<String> = type.generateExamples(context)
}

/**
 * Perform a nested search on this wrapped argument to check for a certain type.
 */
public inline fun <reified T> WrappedArgument<*, *, *, *>.containsType(): Boolean {
    var innerType: Argument<*, *> = this

    while (innerType is WrappedArgument<*, *, *, *>) {
        if (innerType is T)
            return true

        innerType = innerType.type
    }

    return innerType is T
}