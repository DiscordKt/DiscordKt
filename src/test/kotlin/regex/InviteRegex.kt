package regex

import me.jakejmattson.discordkt.util.containsInvite
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InviteRegexTest {
    private val validInvites = listOf(
        "https://discord.gg/discordkttest",
        "https://discord.me/discordkttest",
        "https://discord.io/discordkttest",
        "https://discord.com/invite/discordkttest",
        "https://www.discord.gg/discordkttest",
        "https://www.discord.me/discordkttest",
        "https://www.discord.io/discordkttest",
        "https://www.discord.com/invite/discordkttest",
        "lorem ipsum https://discord.gg/discordkttest lorem ipsum"
    )

    private val invalidInvites = listOf(
        "https://discord.gg",
        "https://discord.me",
        "https://discord.io",
        "https://discord.com"
    )

    @Test
    fun testInvite() {
        validInvites.forEach {
            assertTrue(it.containsInvite())
        }

        invalidInvites.forEach {
            assertTrue(!it.containsInvite())
        }
    }
}