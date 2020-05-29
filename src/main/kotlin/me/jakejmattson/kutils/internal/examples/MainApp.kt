package me.jakejmattson.kutils.internal.examples

import me.aberrantfox.kutils.api.dsl.configuration.startBot
import java.awt.Color

fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: throw IllegalArgumentException("No program arguments provided. Expected bot token.")

    startBot(token, enableScriptEngine = true) {
        configure {
            //The prefix for commands that your bot will respond to
            prefix {
                "!"
            }

            //Whether or not mentioning the bot can be used as a prefix
            allowMentionPrefix = false

            //The emoji that the bot will react on invocations; null for none
            commandReaction = "\uD83D\uDC40"

            //Whether or not error messages should be deleted after sending
            deleteErrors = false

            //Whether or not commands are only valid in guilds
            requiresGuild = true

            //Color configuration for embeds within KUtils
            colors {
                successColor = Color.GREEN
                failureColor = Color.RED
                infoColor = Color.BLUE
            }

            //An embed produced when the bot is mentioned
            mentionEmbed { event ->
                val self = event.discord.jda.selfUser

                color = Color(0x00bfff)
                thumbnail = self.effectiveAvatarUrl
                addInlineField("Prefix", event.relevantPrefix)

                with(discord.properties) {
                    addField("Build Info", "```" +
                        "KUtils: $kutilsVersion\n" +
                        "Kotlin: $kotlinVersion\n" +
                        "JDA:    $jdaVersion\n" +
                        "```")

                    addInlineField("Source", repository)
                }
            }

            //A predicate to determine if a command is visible in this context
            visibilityPredicate {
                it.command.names.first().length < 50
            }
        }
    }
}