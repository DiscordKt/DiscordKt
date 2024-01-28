import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import me.jakejmattson.discordkt.util.containsInvite

class InviteRegex : DescribeSpec({
    describe("Valid Invites") {
        listOf(
            "https://discord.gg/discordkttest",
            "https://discord.me/discordkttest",
            "https://discord.io/discordkttest",
            "https://discord.com/invite/discordkttest",
            "https://www.discord.gg/discordkttest",
            "https://www.discord.me/discordkttest",
            "https://www.discord.io/discordkttest",
            "https://www.discord.com/invite/discordkttest",
            "lorem ipsum https://discord.gg/discordkttest lorem ipsum"
        ).forEach { input ->
            it(input) {
                input.containsInvite().shouldBeTrue()
            }
        }
    }

    describe("Invalid Invites") {
        listOf(
            "https://discord.gg",
            "https://discord.me",
            "https://discord.io",
            "https://discord.com"
        ).forEach { input ->
            it(input) {
                input.containsInvite().shouldBeFalse()
            }
        }
    }
})