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
    var helpCategory : String
    var helpDescription: String
    var helpEmbedDescription: String

    var unknownCommand: String

    //Errors
    @RequiresFill(["The closest command name"])
    var commandRecommendation: String
}

data class LocaleEN(
    override var helpName: String = "Help",
    override var helpCategory: String = "Utility",
    override var helpDescription: String = "Display a help menu.",
    override var helpEmbedDescription: String = "Use `${helpName} <command>` for more information.",

    override var unknownCommand: String = "Unknown Command",

    override var commandRecommendation: String = "Recommendation: %s"

) : Locale