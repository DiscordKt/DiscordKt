package me.jakejmattson.discordkt.api.locale

/**
 * English locale pack
 */
public data class LocaleEN(
    public override var helpName: String = "Help",
    public override var helpCategory: String = "Utility",
    public override var helpDescription: String = "Display a help menu.",
    public override var helpEmbedDescription: String = "Use `${helpName} <command>` for more information.",

    public override var unknownCommand: String = "Unknown Command",
    public override var notFound: String = "Not found",
    public override var invalidFormat: String = "Invalid format",

    public override var commandRecommendation: String = "Recommendation: {0}",
    public override var badArgs: String = "Cannot execute `{0}` with these args.",
    public override var invalidBooleanArg: String = "Must be '{0}' or '{1}'",

    public override var anyArgDescription: String = "A single word or value",
    public override var attachmentArgDescription: String = "A Discord attachment",
    public override var booleanArgDescription: String = "Either {0} or {1}",
    public override var categoryArgDescription: String = "A Discord category",
    public override var channelArgDescription: String = "A Discord channel",
    public override var charArgDescription: String = "A single letter",
    public override var choiceArgDescription: String = "A list to choose from",
    public override var commandArgDescription: String = "A DiscordKt command",
    public override var doubleArgDescription: String = "A decimal number",
    public override var eitherArgDescription: String = "Either {0} or {1}",
    public override var everyArgDescription: String = "All remaining input",
    public override var guildArgDescription: String = "A Discord guild",
    public override var guildEmojiArgDescription: String = "A Discord emoji",
    public override var hexColorArgDescription: String = "A hexadecimal color",
    public override var integerArgDescription: String = "A whole number",
    public override var integerRangeArgDescription: String = "A whole number between {0} and {1}",
    public override var longArgDescription: String = "A whole number",
    public override var memberArgDescription: String = "A Discord member",
    public override var messageArgDescription: String = "A Discord message",
    public override var multipleArgDescription: String = "Any number of {0}",
    public override var optionalArgDescription: String = "[optional] {0}",
    public override var quoteArgDescription: String = "Text between quotations",
    public override var roleArgDescription: String = "A Discord role",
    public override var splitterArgDescription: String = "Items split by {0}",
    public override var timeArgDescription: String = "An amount of time",
    public override var unicodeEmojiArgDescription: String = "A simple emoji",
    public override var urlArgDescription: String = "A URL",
    public override var userArgDescription: String = "A Discord user"
) : Locale