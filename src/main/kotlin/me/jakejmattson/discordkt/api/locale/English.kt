package me.jakejmattson.discordkt.api.locale

/**
 * English locale pack
 */
public data class LocaleEN(
    override public var helpName: String = "Help",
    override public var helpCategory: String = "Utility",
    override public var helpDescription: String = "Display a help menu.",
    override public var helpEmbedDescription: String = "Use `${helpName} <command>` for more information.",

    override public var unknownCommand: String = "Unknown Command",
    override public var notFound: String = "Not found",
    override public var invalidFormat: String = "Invalid format",

    override public var commandRecommendation: String = "Recommendation: {0}",
    override public var badArgs: String = "Cannot execute `{0}` with these args.",
    override public var invalidBooleanArg: String = "Must be '{0}' or '{1}'",

    override public var anyArgDescription: String = "A single word or value",
    override public var attachmentArgDescription: String = "A Discord attachment",
    override public var booleanArgDescription: String = "Either {0} or {1}",
    override public var categoryArgDescription: String = "A Discord category",
    override public var channelArgDescription: String = "A Discord channel",
    override public var charArgDescription: String = "A single letter",
    override public var choiceArgDescription: String = "A list to choose from",
    override public var commandArgDescription: String = "A DiscordKt command",
    override public var doubleArgDescription: String = "A decimal number",
    override public var eitherArgDescription: String = "Either {0} or {1}",
    override public var everyArgDescription: String = "All remaining input",
    override public var guildArgDescription: String = "A Discord guild",
    override public var guildEmojiArgDescription: String = "A Discord emoji",
    override public var hexColorArgDescription: String = "A hexadecimal color",
    override public var integerArgDescription: String = "A whole number",
    override public var integerRangeArgDescription: String = "A whole number between {0} and {1}",
    override public var longArgDescription: String = "A whole number",
    override public var memberArgDescription: String = "A Discord member",
    override public var messageArgDescription: String = "A Discord message",
    override public var multipleArgDescription: String = "Any number of {0}",
    override public var optionalArgDescription: String = "[optional] {0}",
    override public var quoteArgDescription: String = "Text between quotations",
    override public var roleArgDescription: String = "A Discord role",
    override public var splitterArgDescription: String = "Items split by {0}",
    override public var timeArgDescription: String = "An amount of time",
    override public var unicodeEmojiArgDescription: String = "A simple emoji",
    override public var urlArgDescription: String = "A URL",
    override public var userArgDescription: String = "A Discord user"
) : Locale