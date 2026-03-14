package me.jakejmattson.discordkt.arguments

import dev.kord.common.entity.ALL
import dev.kord.common.entity.Permissions
import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.commands.GuildSlashCommand
import me.jakejmattson.discordkt.commands.GuildTextCommand
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.discord
import me.jakejmattson.discordkt.util.discordContext
import me.jakejmattson.discordkt.util.transform

class CommandArgTest : DescribeSpec({
    val commands = mutableListOf(
        GuildTextCommand(names = listOf("a", "b"), category = "test", requiredPermissions = Permissions.ALL),
        GuildSlashCommand(name = "slash", description = "", category = "test", requiredPermissions = Permissions.ALL),
    )

    val discordContext = discordContext(
        _discord = discord(_commands = commands)
    )

    describe("when the input is valid") {
        transform(CommandArg, discordContext) {
            "a" becomes commands.first()
            "b" becomes commands.first()
            "slash" becomes commands.last()
        }
    }

    describe("when the input is invalid") {
        transform(CommandArg, discordContext) {
            "fake" producesError internalLocale.notFound
        }
    }
})
