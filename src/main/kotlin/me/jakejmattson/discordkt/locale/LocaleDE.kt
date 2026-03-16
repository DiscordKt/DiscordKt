package me.jakejmattson.discordkt.locale


/**
 * German locale pack
 */
public data class LocaleDE(
    public override var helpName: String = "Hilfe",
    public override var helpCategory: String = "Dienstprogramm",
    public override var helpDescription: String = "Zeigt ein Hilfemenü an.",
    public override var helpEmbedDescription: String = "Verwende `${helpName} <Befehl>` für weitere Informationen.",

    public override var unknownCommand: String = "Unbekannter Befehl",
    public override var notFound: String = "Nicht gefunden",
    public override var invalidFormat: String = "Ungültiges Format",

    public override var commandRecommendation: String = "Empfehlung: {0}",
    public override var badArgs: String = "Kann `{0}` mit diesen Argumenten nicht ausführen.",
    public override var invalidBooleanArg: String = "Muss '{0}' oder '{1}' sein",

    public override var anyArgDescription: String = "Ein einzelnes Wort oder Wert",
    public override var attachmentArgDescription: String = "Ein Discord-Anhang",
    public override var booleanArgDescription: String = "Entweder {0} oder {1}",
    public override var channelArgDescription: String = "Ein Discord-Kanal",
    public override var charArgDescription: String = "Ein einzelner Buchstabe",
    public override var choiceArgDescription: String = "Eine Liste zur Auswahl",
    public override var commandArgDescription: String = "Ein DiscordKt-Befehl",
    public override var doubleArgDescription: String = "Eine Dezimalzahl",
    public override var everyArgDescription: String = "Der gesamte verbleibende Input",
    public override var guildArgDescription: String = "Eine Discord-Gilde",
    public override var guildEmojiArgDescription: String = "Ein Discord-Emoji",
    public override var hexColorArgDescription: String = "Eine hexadezimale Farbe",
    public override var integerArgDescription: String = "Eine ganze Zahl",
    public override var integerRangeArgDescription: String = "Eine ganze Zahl zwischen {0} und {1}",
    public override var memberArgDescription: String = "Ein Discord-Mitglied",
    public override var messageArgDescription: String = "Eine Discord-Nachricht",
    public override var multipleArgDescription: String = "Beliebige Anzahl von {0}",
    public override var optionalArgDescription: String = "[optional] {0}",
    public override var quoteArgDescription: String = "Text in Anführungszeichen",
    public override var roleArgDescription: String = "Eine Discord-Rolle",
    public override var splitterArgDescription: String = "Elemente getrennt durch {0}",
    public override var timeArgDescription: String = "Eine Zeitangabe",
    public override var unicodeEmojiArgDescription: String = "Ein einfaches Emoji",
    public override var urlArgDescription: String = "Eine URL",
    public override var userArgDescription: String = "Ein Discord-Benutzer"
) : Locale
