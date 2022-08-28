package me.jakejmattson.discordkt.locale

/**
 * Spanish locale pack
 */
public data class LocaleES(
    public override var helpName: String = "Ayuda",
    public override var helpCategory: String = "Utilidad",
    public override var helpDescription: String = "Muestra un menú de auyda.",
    public override var helpEmbedDescription: String = "Utilice `${helpName} <command>` para más información.",

    public override var unknownCommand: String = "Comando Desconocido",
    public override var notFound: String = "No encontrado",
    public override var invalidFormat: String = "Formato inválido",

    public override var commandRecommendation: String = "Recomendación: {0}",
    public override var badArgs: String = "No se puede ejecutar `{0}` con estos argumentos.",
    public override var invalidBooleanArg: String = "Debe ser '{0}' o '{1}'",

    public override var anyArgDescription: String = "Una sola palabra o valor",
    public override var attachmentArgDescription: String = "Un archivo adjunto de Discord",
    public override var booleanArgDescription: String = "O bien {0} or {1}",
    public override var channelArgDescription: String = "Un canal de Discord",
    public override var charArgDescription: String = "Una sola letra",
    public override var choiceArgDescription: String = "Una lista para elegir",
    public override var commandArgDescription: String = "Un comando de DiscordKt",
    public override var doubleArgDescription: String = "Un número decimal",
    public override var everyArgDescription: String = "Todos los input restantes",
    public override var guildArgDescription: String = "Un servidor de Discord",
    public override var guildEmojiArgDescription: String = "Un emoji de Discord",
    public override var hexColorArgDescription: String = "Un color hexadecimal",
    public override var integerArgDescription: String = "Un número entero",
    public override var integerRangeArgDescription: String = "Un número entero entre {0} y {1}",
    public override var memberArgDescription: String = "Un miembro de Discord",
    public override var messageArgDescription: String = "Un mensaje de Discord",
    public override var multipleArgDescription: String = "Cualquier número múltiplo de {0}",
    public override var optionalArgDescription: String = "[optional] {0}",
    public override var quoteArgDescription: String = "Texto entre comillas",
    public override var roleArgDescription: String = "Un rol de Discord",
    public override var splitterArgDescription: String = "Items divididos por {0}",
    public override var timeArgDescription: String = "Una cantidad de tiempo",
    public override var unicodeEmojiArgDescription: String = "Un emoji simple",
    public override var urlArgDescription: String = "Una URL",
    public override var userArgDescription: String = "Un usuario de Discord"
) : Locale
