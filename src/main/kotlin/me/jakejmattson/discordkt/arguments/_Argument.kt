package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.right
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.DiscordContext

/**
 * An object that represents a type and contains the logic to convert string arguments to the desired type.
 *
 * @property name The display name for this type in documentations and examples.
 * @property description A description of the data that this type represents.
 */
public sealed interface Argument<Input, Output> : Cloneable {
    public val name: String
    public val description: String

    /**
     * Accept multiple inputs of this Argument.
     */
    public fun multiple(): MultipleArg<Input, Output> = MultipleArg(this)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type.
     */
    public fun optional(default: Output): OptionalArg<Input, Output, Output> = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type.
     */
    public fun optional(default: suspend (DiscordContext) -> Output): OptionalArg<Input, Output, Output> =
        OptionalArg(name, this, default)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type - can also be null.
     */
    public fun optionalNullable(default: Output? = null): OptionalArg<Input, Output, Output?> =
        OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type - can also be null.
     */
    public fun optionalNullable(default: suspend (DiscordContext) -> Output?): OptionalArg<Input, Output, Output?> =
        OptionalArg(name, this, default)

    /**
     * Parse string input into the correct type handled by this argument.
     *
     * @param args A list of string arguments.
     * @param discord The [Discord] object used to resolve discord entities.
     */
    public suspend fun parse(args: MutableList<String>, discord: Discord): Input?

    /**
     * Transforms a value produced by a slash command or by the [parse] function.
     *
     * @param input The input data of the type [Input]
     * @param context The [DiscordContext] created by the execution of the command.
     * @return [Result] subtype [Success] or [Error].
     */
    public suspend fun transform(input: Input, context: DiscordContext): Either<String, Output> =
        (input as Output).right()

    /**
     * A function called whenever an example of this type is needed.
     *
     * @param context Allows the list result to be generated with the relevant discord context.
     */
    public suspend fun generateExamples(context: DiscordContext): List<String>

    /**
     * Utility function to check that this Argument is an [OptionalArg].
     */
    public fun isOptional(): Boolean =
        if (this is WrappedArgument<*, *, *, *>) this.containsType<OptionalArg<*, *, *>>() else false
}