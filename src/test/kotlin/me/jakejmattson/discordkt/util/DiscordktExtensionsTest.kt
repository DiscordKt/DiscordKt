package me.jakejmattson.discordkt.util

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.application.GlobalApplicationCommand
import dev.kord.core.entity.application.GuildApplicationCommand
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import me.jakejmattson.discordkt.commands.GlobalSlashCommand
import me.jakejmattson.discordkt.commands.GuildSlashCommand

class DiscordktExtensionsTest : DescribeSpec({

    describe("mentionOrNull") {
        val guild: Guild = mockk()

        describe("A GuildSlashCommand") {
            fun makeSubject(
                getApplicationCommands: Flow<GuildApplicationCommand> = emptyFlow()
            ): GuildSlashCommand = mockk {
                every { name } returns "test"
                every { guild.getApplicationCommands() } returns getApplicationCommands
            }

            describe("When there are no commands") {
                val dktCommand = makeSubject()
                val mention = dktCommand.mentionOrNull(guild)

                it("should be null") {
                    mention.shouldBeNull()
                }
            }

            describe("When there is one command") {
                val discordCommand: GuildApplicationCommand = mockk {
                    every { name } returns "test"
                    every { id } returns Snowflake(0)
                }

                val dktCommand = makeSubject(getApplicationCommands = flowOf(discordCommand))
                val mention = dktCommand.mentionOrNull(guild)

                it("should be a mention") {
                    mention shouldBe "</test:0>"
                }
            }
        }

        describe("A GlobalSlashCommand") {
            fun makeSubject(
                getApplicationCommands: Flow<GlobalApplicationCommand> = emptyFlow()
            ): GlobalSlashCommand = mockk {
                every { name } returns "test"
                every { guild.kord.getGlobalApplicationCommands() } returns getApplicationCommands
            }

            describe("When there are no commands") {
                val dktCommand = makeSubject()
                val mention = dktCommand.mentionOrNull(guild)

                it("should be null") {
                    mention.shouldBeNull()
                }
            }

            describe("When there is one command") {
                val discordCommand: GlobalApplicationCommand = mockk {
                    every { name } returns "test"
                    every { id } returns Snowflake(0)
                }

                val dktCommand = makeSubject(getApplicationCommands = flowOf(discordCommand))
                val mention = dktCommand.mentionOrNull(guild)

                it("should be a mention") {
                    mention shouldBe "</test:0>"
                }
            }
        }
    }
})
