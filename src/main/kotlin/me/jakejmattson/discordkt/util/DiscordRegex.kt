package me.jakejmattson.discordkt.util

/**
 * A collection of [Regex] for detecting discord strings
 */
public object DiscordRegex {
    /** A list of valid web URL regex */
    public val url: List<Regex> = listOf(
        "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
    ).map { it.toRegex() }

    /** A discord invite url */
    public val invite: Regex = Regex("(https?://)?(www\\.)?(discord\\.(gg|me|io|com/invite)/)([^\\s]+)")

    /** A mention of @role */
    public val role: Regex = Regex("<@&(\\d+)>")

    /** A mention of @user */
    public val user: Regex = Regex("<@!?(\\d+)>")

    /** A mention of @here */
    public val here: Regex = Regex("@+here")

    /** A mention of @everyone */
    public val everyone: Regex = Regex("@+everyone")

    /** A public/guild discord message */
    public val publicMessage: Regex = Regex("https://discord\\.com/channels/(\\d{17,21})/(\\d{17,21})/(\\d{17,21})")

    /** A private discord message */
    public val privateMessage: Regex = Regex("https://discord\\.com/channels/@me/(\\d{17,21})/(\\d{17,21})")

    /** A valid slash command/argument name */
    public val slashName: Regex = Regex("^[\\w-]{1,32}$")
}