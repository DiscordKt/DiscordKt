package me.jakejmattson.discordkt.api.locale

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RequiresFill(val requirements: Array<String>)

/**
 * Enum collection of available languages.
 *
 * @property locale The matching locale for this language.
 */
public enum class Language(public val locale: Locale) {
    /** English
     * @sample LocaleEN
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

    /** Invalid input for [BooleanArg][me.jakejmattson.discordkt.api.arguments.BooleanArg]
     * {0} truth value
     * {1} false value
     */
    @RequiresFill(["truth value", "false value"])
    public var invalidBooleanArg: String

    /** [AnyArg][me.jakejmattson.discordkt.api.arguments.AnyArg] description */
    public var anyArgDescription: String

    /** [AttachmentArg][me.jakejmattson.discordkt.api.arguments.AttachmentArg] description */
    public var attachmentArgDescription: String

    /** [BooleanArg][me.jakejmattson.discordkt.api.arguments.BooleanArg] description
     * {0} truth value
     * {1} false value
     */
    @RequiresFill(["truth value", "false value"])
    public var booleanArgDescription: String

    /** [CategoryArg][me.jakejmattson.discordkt.api.arguments.CategoryArg] description */
    public var categoryArgDescription: String

    /** [ChannelArg][me.jakejmattson.discordkt.api.arguments.ChannelArg] description */
    public var channelArgDescription: String

    /** [CharArg][me.jakejmattson.discordkt.api.arguments.CharArg] description */
    public var charArgDescription: String

    /** [ChoiceArg][me.jakejmattson.discordkt.api.arguments.ChoiceArg] description */
    public var choiceArgDescription: String

    /** [CommandArg][me.jakejmattson.discordkt.api.arguments.CommandArg] description */
    public var commandArgDescription: String

    /** [DoubleArg][me.jakejmattson.discordkt.api.arguments.DoubleArg] description */
    public var doubleArgDescription: String

    /** [EitherArg][me.jakejmattson.discordkt.api.arguments.EitherArg] description
     * {0} left type
     * {1} right type
     */
    @RequiresFill(["left type", "right type"])
    public var eitherArgDescription: String

    /** [EveryArg][me.jakejmattson.discordkt.api.arguments.EveryArg] description */
    public var everyArgDescription: String

    /** [GuildArg][me.jakejmattson.discordkt.api.arguments.GuildArg] description */
    public var guildArgDescription: String

    /** [GuildEmojiArg][me.jakejmattson.discordkt.api.arguments.GuildEmojiArg] description */
    public var guildEmojiArgDescription: String

    /** [HexColorArg][me.jakejmattson.discordkt.api.arguments.HexColorArg] description */
    public var hexColorArgDescription: String

    /** [IntegerArg][me.jakejmattson.discordkt.api.arguments.IntegerArg] description */
    public var integerArgDescription: String

    /** [IntegerRangeArg][me.jakejmattson.discordkt.api.arguments.IntegerRangeArg] description
     * {0} minimum value
     * {1} maximum value
     */
    @RequiresFill(["minimum value", "maximum value"])
    public var integerRangeArgDescription: String

    /** [LongArg][me.jakejmattson.discordkt.api.arguments.LongArg] description */
    public var longArgDescription: String

    /** [MemberArg][me.jakejmattson.discordkt.api.arguments.MemberArg] description */
    public var memberArgDescription: String

    /** [MessageArg][me.jakejmattson.discordkt.api.arguments.MessageArg] description */
    public var messageArgDescription: String

    /** [MultipleArg][me.jakejmattson.discordkt.api.arguments.MultipleArg] description
     * {0} type name
     */
    @RequiresFill(["type name"])
    public var multipleArgDescription: String

    /** [OptionalArg][me.jakejmattson.discordkt.api.arguments.OptionalArg] description
     * {0} type name
     */
    @RequiresFill(["type name"])
    public var optionalArgDescription: String

    /** [QuoteArg][me.jakejmattson.discordkt.api.arguments.QuoteArg] description */
    public var quoteArgDescription: String

    /** [RoleArg][me.jakejmattson.discordkt.api.arguments.RoleArg] description */
    public var roleArgDescription: String

    /** [SplitterArg][me.jakejmattson.discordkt.api.arguments.SplitterArg] description
     * {0} splitter character
     */
    @RequiresFill(["splitter character"])
    public var splitterArgDescription: String

    /** [TimeArg][me.jakejmattson.discordkt.api.arguments.TimeArg] description */
    public var timeArgDescription: String

    /** [UnicodeEmojiArg][me.jakejmattson.discordkt.api.arguments.UnicodeEmojiArg] description */
    public var unicodeEmojiArgDescription: String

    /** [UrlArg][me.jakejmattson.discordkt.api.arguments.UrlArg] description */
    public var urlArgDescription: String

    /** [UserArg][me.jakejmattson.discordkt.api.arguments.UserArg] description */
    public var userArgDescription: String
}

internal fun String.inject(vararg args: String) = args.foldIndexed(this) { index: Int, temp: String, arg: String ->
    temp.replace("{$index}", arg)
}