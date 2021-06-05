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

    /** [AnyArg][me.jakejmattson.discordkt.api.arguments.AnyArg] description */
    var anyArgDescription: String

    /** [AttachmentArg][me.jakejmattson.discordkt.api.arguments.AttachmentArg] description */
    var attachmentArgDescription: String

    /** [BooleanArg][me.jakejmattson.discordkt.api.arguments.BooleanArg] description */
    @RequiresFill(["truth value", "false value"])
    var booleanArgDescription: String

    /** [CategoryArg][me.jakejmattson.discordkt.api.arguments.CategoryArg] description */
    var categoryArgDescription: String

    /** [ChannelArg][me.jakejmattson.discordkt.api.arguments.ChannelArg] description */
    var channelArgDescription: String

    /** [CharArg][me.jakejmattson.discordkt.api.arguments.CharArg] description */
    var charArgDescription: String

    /** [ChoiceArg][me.jakejmattson.discordkt.api.arguments.ChoiceArg] description */
    var choiceArgDescription: String

    /** [CommandArg][me.jakejmattson.discordkt.api.arguments.CommandArg] description */
    var commandArgDescription: String

    /** [DoubleArg][me.jakejmattson.discordkt.api.arguments.DoubleArg] description */
    var doubleArgDescription: String

    /** [EitherArg][me.jakejmattson.discordkt.api.arguments.EitherArg] description */
    @RequiresFill(["left type", "right type"])
    var eitherArgDescription: String

    /** [EveryArg][me.jakejmattson.discordkt.api.arguments.EveryArg] description */
    var everyArgDescription: String

    /** [GuildArg][me.jakejmattson.discordkt.api.arguments.GuildArg] description */
    var guildArgDescription: String

    /** [GuildEmojiArg][me.jakejmattson.discordkt.api.arguments.GuildEmojiArg] description */
    var guildEmojiArgDescription: String

    /** [HexColorArg][me.jakejmattson.discordkt.api.arguments.HexColorArg] description */
    var hexColorArgDescription: String

    /** [IntegerArg][me.jakejmattson.discordkt.api.arguments.IntegerArg] description */
    var integerArgDescription: String

    /** [IntegerRangeArg][me.jakejmattson.discordkt.api.arguments.IntegerRangeArg] description */
    @RequiresFill(["minimum value", "maximum value"])
    var integerRangeArgDescription: String

    /** [LongArg][me.jakejmattson.discordkt.api.arguments.LongArg] description */
    var longArgDescription: String

    /** [MemberArg][me.jakejmattson.discordkt.api.arguments.MemberArg] description */
    var memberArgDescription: String

    /** [MessageArg][me.jakejmattson.discordkt.api.arguments.MessageArg] description */
    var messageArgDescription: String

    /** [MultipleArg][me.jakejmattson.discordkt.api.arguments.MultipleArg] description */
    @RequiresFill(["multiple type"])
    var multipleArgDescription: String

    /** [OptionalArg][me.jakejmattson.discordkt.api.arguments.OptionalArg] description */
    @RequiresFill(["optional type"])
    var optionalArgDescription: String

    /** [QuoteArg][me.jakejmattson.discordkt.api.arguments.QuoteArg] description */
    var quoteArgDescription: String

    /** [RoleArg][me.jakejmattson.discordkt.api.arguments.RoleArg] description */
    var roleArgDescription: String

    /** [SplitterArg][me.jakejmattson.discordkt.api.arguments.SplitterArg] description */
    @RequiresFill(["splitter character"])
    var splitterArgDescription: String

    /** [TimeArg][me.jakejmattson.discordkt.api.arguments.TimeArg] description */
    var timeArgDescription: String

    /** [UnicodeEmojiArg][me.jakejmattson.discordkt.api.arguments.UnicodeEmojiArg] description */
    var unicodeEmojiArgDescription: String

    /** [UrlArg][me.jakejmattson.discordkt.api.arguments.UrlArg] description */
    var urlArgDescription: String

    /** [UserArg][me.jakejmattson.discordkt.api.arguments.UserArg] description */
    var userArgDescription: String
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
    override var invalidBooleanArg: String = "Must be '{0}' or '{1}'",

    override var anyArgDescription: String = "A single word or value",
    override var attachmentArgDescription: String = "A Discord attachment",
    override var booleanArgDescription: String = "Either {0} or {1}",
    override var categoryArgDescription: String = "A Discord category",
    override var channelArgDescription: String = "A Discord channel",
    override var charArgDescription: String = "A single letter",
    override var choiceArgDescription: String = "A list to choose from",
    override var commandArgDescription: String = "A DiscordKt command",
    override var doubleArgDescription: String = "A Discord member",
    override var eitherArgDescription: String = "Either {0} or ${1}",
    override var everyArgDescription: String = "All input",
    override var guildArgDescription: String = "A Discord guild",
    override var guildEmojiArgDescription: String = "A Discord emoji",
    override var hexColorArgDescription: String = "A hexadecimal color",
    override var integerArgDescription: String = "A whole number",
    override var integerRangeArgDescription: String = "A whole number between {0} and {1}",
    override var longArgDescription: String = "A whole number",
    override var memberArgDescription: String = "A Discord member",
    override var messageArgDescription: String = "A Discord message",
    override var multipleArgDescription: String = "Any number of {0}",
    override var optionalArgDescription: String = "An optional {0}",
    override var quoteArgDescription: String = "Text between quotations",
    override var roleArgDescription: String = "A Discord role",
    override var splitterArgDescription: String = "Items split by {0}",
    override var timeArgDescription: String = "An amount of time",
    override var unicodeEmojiArgDescription: String = "A simple emoji",
    override var urlArgDescription: String = "A URL (link)",
    override var userArgDescription: String = "A Discord user"
) : Locale

internal fun String.inject(vararg args: String) = args.foldIndexed(this) { index: Int, temp: String, arg: String ->
    temp.replace("{$index}", arg)
}