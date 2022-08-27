package me.jakejmattson.discordkt.locale

/**
 * Turkish locale pack
 */
public data class LocaleTR(
    public override var helpName: String = "Yardım",
    public override var helpCategory: String = "Faydalılar",
    public override var helpDescription: String = "Yardım menüsünü gösterir.",
    public override var helpEmbedDescription: String = "`${helpName} <komut>` yazarak yardım alabilirsiniz.",

    public override var unknownCommand: String = "Bilinmeyen Komut",
    public override var notFound: String = "Bulunamadı",
    public override var invalidFormat: String = "Hatalı format",

    public override var commandRecommendation: String = "Öneri: {0}",
    public override var badArgs: String = "`{0}` komudu bu argümanlarla kullanılamıyor.",
    public override var invalidBooleanArg: String = "'{0}' ya da '{1}' olmalı",

    public override var anyArgDescription: String = "Tek bir kelime ya da değer",
    public override var attachmentArgDescription: String = "Bir Discord dosyası",
    public override var booleanArgDescription: String = "{0} ya da {1}",
    public override var channelArgDescription: String = "Bir Discord kanalı",
    public override var charArgDescription: String = "Tek bir harf",
    public override var choiceArgDescription: String = "Aralarından seçim yapabileceğiniz bir liste",
    public override var commandArgDescription: String = "Bir DiscordKt komudu",
    public override var doubleArgDescription: String = "Bir ondalıklı sayı",
    public override var everyArgDescription: String = "Kalan tüm girdiler",
    public override var guildArgDescription: String = "Bir Discord sunucusu",
    public override var guildEmojiArgDescription: String = "Bir Discord emojisi",
    public override var hexColorArgDescription: String = "Bir hexadecimal renk",
    public override var integerArgDescription: String = "Bir tam sayı",
    public override var integerRangeArgDescription: String = "{0} ve {1} arasında bir tam sayı",
    public override var memberArgDescription: String = "Bir Discord üyesi",
    public override var messageArgDescription: String = "Bir Discord mesajı",
    public override var multipleArgDescription: String = "Herhangi bir sayıda {0}",
    public override var optionalArgDescription: String = "[opsiyonel] {0}",
    public override var quoteArgDescription: String = "Tırnak içinde bir yazı",
    public override var roleArgDescription: String = "Bir Discord rolü",
    public override var splitterArgDescription: String = "{0} ile ayrılan öğeler",
    public override var timeArgDescription: String = "Bir miktar zaman",
    public override var unicodeEmojiArgDescription: String = "Basit bir emoji",
    public override var urlArgDescription: String = "Bir URL",
    public override var userArgDescription: String = "Bir Discord kullanıcısı"
) : Locale