package me.jakejmattson.discordkt.api.locale

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RequiresFill(val requirements: Array<String>)

/**
 * Enum collection of available languages.
 *
 * @property locale The matching locale value for this language.
 */
enum class Language(val locale: Locale) {
    /** English */
    EN(LocaleEN());
}

/**
 * Customizable Strings in DiscordKt
 */
interface Locale {
    //Help Command
    /** The name of the help command */
    var helpName: String

    /** The category of the help command */
    var helpCategory: String

    /** The description of the help command */
    var helpDescription: String

    /** The description used in the help command embed */
    var helpEmbedDescription: String

    /** Literal text */
    var unknownCommand: String

    /** Literal text */
    var notFound: String

    /** Literal text */
    var invalidFormat: String

    //Errors
    /** A string recommending the command with the neatest name */
    @RequiresFill(["The closest command name"])
    var commandRecommendation: String

    /** Command was provided with invalid arguments */
    @RequiresFill(["The command name attempted to run"])
    var badArgs: String

    @RequiresFill(["truth value", "false value"])
    /** An error response for [me.jakejmattson.discordkt.api.arguments.BooleanArg]*/
    var invalidBooleanArg: String
}

/**
 * English locale pack
 */
data class LocaleEN(
    override var helpName: String = "Help",
    override var helpCategory: String = "Utility",
    override var helpDescription: String = "Display a help menu.",
    override var helpEmbedDescription: String = "Use `${helpName} <command>` for more information.",

    override var unknownCommand: String = "Unknown Command",
    override var notFound: String = "Not found",
    override var invalidFormat: String = "Invalid format",

    override var commandRecommendation: String = "Recommendation: {0}",
    override var badArgs: String = "Cannot execute `{0}` with these args.",
    override var invalidBooleanArg: String = "Must be '{0}' or '{1}'"
) : Locale

internal fun String.inject(vararg args: String) = args.foldIndexed(this) { index: Int, temp: String, arg: String ->
    temp.replace("{$index}", arg)
}