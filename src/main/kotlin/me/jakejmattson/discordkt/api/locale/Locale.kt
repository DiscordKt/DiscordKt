package me.jakejmattson.discordkt.api.locale

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RequiresFill(val requirements: Array<String>)

/**
 * Enum collection of available languages.
 *
 * @property locale The matching locale for this language.
 */
enum class Language(val locale: Locale) {
    /** English
     * @sample LocaleEN
     */
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
    /** A string recommending the command with the nearest name
     * {0} command name
     */
    @RequiresFill(["command name"])
    var commandRecommendation: String

    /** Command was provided with invalid arguments
     * {0} command name
     */
    @RequiresFill(["command name"])
    var badArgs: String

    /** Invalid input for [BooleanArg][me.jakejmattson.discordkt.api.arguments.BooleanArg]
     * {0} truth value
     * {1} false value
     */
    @RequiresFill(["truth value", "false value"])
    var invalidBooleanArg: String

    /** [AnyArg][me.jakejmattson.discordkt.api.arguments.AnyArg] description */
    var anyArgDescription: String

    /** [AttachmentArg][me.jakejmattson.discordkt.api.arguments.AttachmentArg] description */
    var attachmentArgDescription: String

    /** [BooleanArg][me.jakejmattson.discordkt.api.arguments.BooleanArg] description
     * {0} truth value
     * {1} false value
     */
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

    /** [EitherArg][me.jakejmattson.discordkt.api.arguments.EitherArg] description
     * {0} left type
     * {1} right type
     */
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

    /** [IntegerRangeArg][me.jakejmattson.discordkt.api.arguments.IntegerRangeArg] description
     * {0} minimum value
     * {1} maximum value
     */
    @RequiresFill(["minimum value", "maximum value"])
    var integerRangeArgDescription: String

    /** [LongArg][me.jakejmattson.discordkt.api.arguments.LongArg] description */
    var longArgDescription: String

    /** [MemberArg][me.jakejmattson.discordkt.api.arguments.MemberArg] description */
    var memberArgDescription: String

    /** [MessageArg][me.jakejmattson.discordkt.api.arguments.MessageArg] description */
    var messageArgDescription: String

    /** [MultipleArg][me.jakejmattson.discordkt.api.arguments.MultipleArg] description
     * {0} type name
     */
    @RequiresFill(["type name"])
    var multipleArgDescription: String

    /** [OptionalArg][me.jakejmattson.discordkt.api.arguments.OptionalArg] description
     * {0} type name
     */
    @RequiresFill(["type name"])
    var optionalArgDescription: String

    /** [QuoteArg][me.jakejmattson.discordkt.api.arguments.QuoteArg] description */
    var quoteArgDescription: String

    /** [RoleArg][me.jakejmattson.discordkt.api.arguments.RoleArg] description */
    var roleArgDescription: String

    /** [SplitterArg][me.jakejmattson.discordkt.api.arguments.SplitterArg] description
     * {0} splitter character
     */
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

internal fun String.inject(vararg args: String) = args.foldIndexed(this) { index: Int, temp: String, arg: String ->
    temp.replace("{$index}", arg)
}