package me.jakejmattson.discordkt.api.dsl

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

interface Locale {
    //Help Command
    var helpName: String
    var helpCategory: String
    var helpDescription: String
    var helpEmbedDescription: String

    var unknownCommand: String

    //Errors
    @RequiresFill(["The closest command name"])
    var commandRecommendation: String

    @RequiresFill(["The command name attempted to run"])
    var badArgs: String
}

data class LocaleEN(
    override var helpName: String = "Help",
    override var helpCategory: String = "Utility",
    override var helpDescription: String = "Display a help menu.",
    override var helpEmbedDescription: String = "Use `${helpName} <command>` for more information.",

    override var unknownCommand: String = "Unknown Command",

    override var commandRecommendation: String = "Recommendation: {0}",
    override var badArgs: String = "Cannot execute `{0}` with these args."
) : Locale

fun String.inject(vararg args: String) = args.foldIndexed(this) { index: Int, temp: String, arg: String ->
    temp.replace("{$index}", arg)
}