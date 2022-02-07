package me.jakejmattson.discordkt.locale

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RequiresFill(val requirements: Array<String>)

/**
 * Enum collection of available languages.
 *
 * @property locale The matching locale for this language.
 */
public enum class Language(public val locale: Locale) {
    /**
     * English
     * @sample me.jakejmattson.discordkt.locale.LocaleEN
     */
    EN(LocaleEN());
}

/**
 * Customizable Strings in DiscordKt
 */
public interface Locale {
    //Help Command
    /** The name of the help command */
    public var helpName: String

    /** The category of the help command */
    public var helpCategory: String

    /** The description of the help command */
    public var helpDescription: String

    /** The description used in the help command embed */
    public var helpEmbedDescription: String

    /** Literal text */
    public var unknownCommand: String

    /** Literal text */
    public var notFound: String

    /** Literal text */
    public var invalidFormat: String

    //Errors
    /** A string recommending the command with the nearest name
     * {0} command name
     */
    @RequiresFill(["command name"])
    public var commandRecommendation: String

    /** Command was provided with invalid arguments
     * {0} command name
     */
    @RequiresFill(["command name"])
    public var badArgs: String

    /** Invalid input for [BooleanArg][me.jakejmattson.discordkt.arguments.BooleanArg]
     * {0} truth value
     * {1} false value
     */
    @RequiresFill(["truth value", "false value"])
    public var invalidBooleanArg: String

    /** [AnyArg][me.jakejmattson.discordkt.arguments.AnyArg] description */
    public var anyArgDescription: String

    /** [AttachmentArg][me.jakejmattson.discordkt.arguments.AttachmentArg] description */
    public var attachmentArgDescription: String

    /** [BooleanArg][me.jakejmattson.discordkt.arguments.BooleanArg] description
     * {0} truth value
     * {1} false value
     */
    @RequiresFill(["truth value", "false value"])
    public var booleanArgDescription: String

    /** [CategoryArg][me.jakejmattson.discordkt.arguments.CategoryArg] description */
    public var categoryArgDescription: String

    /** [ChannelArg][me.jakejmattson.discordkt.arguments.ChannelArg] description */
    public var channelArgDescription: String

    /** [CharArg][me.jakejmattson.discordkt.arguments.CharArg] description */
    public var charArgDescription: String

    /** [ChoiceArg][me.jakejmattson.discordkt.arguments.ChoiceArg] description */
    public var choiceArgDescription: String

    /** [CommandArg][me.jakejmattson.discordkt.arguments.CommandArg] description */
    public var commandArgDescription: String

    /** [DoubleArg][me.jakejmattson.discordkt.arguments.DoubleArg] description */
    public var doubleArgDescription: String

    /** [EitherArg][me.jakejmattson.discordkt.arguments.EitherArg] description
     * {0} left type
     * {1} right type
     */
    @RequiresFill(["left type", "right type"])
    public var eitherArgDescription: String

    /** [EveryArg][me.jakejmattson.discordkt.arguments.EveryArg] description */
    public var everyArgDescription: String

    /** [GuildArg][me.jakejmattson.discordkt.arguments.GuildArg] description */
    public var guildArgDescription: String

    /** [GuildEmojiArg][me.jakejmattson.discordkt.arguments.GuildEmojiArg] description */
    public var guildEmojiArgDescription: String

    /** [HexColorArg][me.jakejmattson.discordkt.arguments.HexColorArg] description */
    public var hexColorArgDescription: String

    /** [IntegerArg][me.jakejmattson.discordkt.arguments.IntegerArg] description */
    public var integerArgDescription: String

    /** [IntegerRangeArg][me.jakejmattson.discordkt.arguments.IntegerRangeArg] description
     * {0} minimum value
     * {1} maximum value
     */
    @RequiresFill(["minimum value", "maximum value"])
    public var integerRangeArgDescription: String

    /** [LongArg][me.jakejmattson.discordkt.arguments.LongArg] description */
    public var longArgDescription: String

    /** [MemberArg][me.jakejmattson.discordkt.arguments.MemberArg] description */
    public var memberArgDescription: String

    /** [MessageArg][me.jakejmattson.discordkt.arguments.MessageArg] description */
    public var messageArgDescription: String

    /** [MultipleArg][me.jakejmattson.discordkt.arguments.MultipleArg] description
     * {0} type name
     */
    @RequiresFill(["type name"])
    public var multipleArgDescription: String

    /** [OptionalArg][me.jakejmattson.discordkt.arguments.OptionalArg] description
     * {0} type name
     */
    @RequiresFill(["type name"])
    public var optionalArgDescription: String

    /** [QuoteArg][me.jakejmattson.discordkt.arguments.QuoteArg] description */
    public var quoteArgDescription: String

    /** [RoleArg][me.jakejmattson.discordkt.arguments.RoleArg] description */
    public var roleArgDescription: String

    /** [SplitterArg][me.jakejmattson.discordkt.arguments.SplitterArg] description
     * {0} splitter character
     */
    @RequiresFill(["splitter character"])
    public var splitterArgDescription: String

    /** [TimeArg][me.jakejmattson.discordkt.arguments.TimeArg] description */
    public var timeArgDescription: String

    /** [UnicodeEmojiArg][me.jakejmattson.discordkt.arguments.UnicodeEmojiArg] description */
    public var unicodeEmojiArgDescription: String

    /** [UrlArg][me.jakejmattson.discordkt.arguments.UrlArg] description */
    public var urlArgDescription: String

    /** [UserArg][me.jakejmattson.discordkt.arguments.UserArg] description */
    public var userArgDescription: String
}

internal fun String.inject(vararg args: String) = args.foldIndexed(this) { index: Int, temp: String, arg: String ->
    temp.replace("{$index}", arg)
}