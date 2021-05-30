package me.jakejmattson.discordkt.api.dsl

enum class Language(val locale: Locale) {
    EN(LocaleEN());
}

interface Locale {
    var helpName: String
    var helpCategory : String
    var helpDescription: String
    var helpEmbedDescription: String
}

data class LocaleEN(
    override var helpName: String = "Help",
    override var helpCategory: String = "Utility",
    override var helpDescription: String = "Display a help menu.",
    override var helpEmbedDescription: String = "Use `${helpName} <command>` for more information."
) : Locale