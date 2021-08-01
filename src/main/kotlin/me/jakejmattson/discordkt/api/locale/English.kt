package me.jakejmattson.discordkt.api.locale

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
    override var invalidBooleanArg: String = "Must be '{0}' or '{1}'",

    override var anyArgDescription: String = "A single word or value",
    override var attachmentArgDescription: String = "A Discord attachment",
    override var booleanArgDescription: String = "Either {0} or {1}",
    override var categoryArgDescription: String = "A Discord category",
    override var channelArgDescription: String = "A Discord channel",
    override var charArgDescription: String = "A single letter",
    override var choiceArgDescription: String = "A list to choose from",
    override var commandArgDescription: String = "A DiscordKt command",
    override var doubleArgDescription: String = "A decimal number",
    override var eitherArgDescription: String = "Either {0} or {1}",
    override var everyArgDescription: String = "All remaining input",
    override var guildArgDescription: String = "A Discord guild",
    override var guildEmojiArgDescription: String = "A Discord emoji",
    override var hexColorArgDescription: String = "A hexadecimal color",
    override var integerArgDescription: String = "A whole number",
    override var integerRangeArgDescription: String = "A whole number between {0} and {1}",
    override var longArgDescription: String = "A whole number",
    override var memberArgDescription: String = "A Discord member",
    override var messageArgDescription: String = "A Discord message",
    override var multipleArgDescription: String = "Any number of {0}",
    override var optionalArgDescription: String = "[optional] {0}",
    override var quoteArgDescription: String = "Text between quotations",
    override var roleArgDescription: String = "A Discord role",
    override var splitterArgDescription: String = "Items split by {0}",
    override var timeArgDescription: String = "An amount of time",
    override var unicodeEmojiArgDescription: String = "A simple emoji",
    override var urlArgDescription: String = "A URL",
    override var userArgDescription: String = "A Discord user"
) : Locale