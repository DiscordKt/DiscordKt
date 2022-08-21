package me.jakejmattson.discordkt.extensions

public object DiscordRegex {
    public val url: List<Regex> = listOf(
        "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
    ).map { it.toRegex() }

    public val invite: Regex = Regex("(https?://)?(www\\.)?(discord\\.(gg|me|io|com/invite)/)([^\\s]+)")
    public val role: Regex = Regex("<@&(\\d+)>")
    public val user: Regex = Regex("<@!?(\\d+)>")
    public val here: Regex = Regex("@+here")
    public val everyone: Regex = Regex("@+everyone")
    public val publicMessage: Regex = Regex("https://discord\\.com/channels/(\\d{17,21})/(\\d{17,21})/(\\d{17,21})")
    public val privateMessage: Regex = Regex("https://discord\\.com/channels/@me/(\\d{17,21})/(\\d{17,21})")
    public val slashName: Regex = Regex("^[\\w-]{1,32}$")
}