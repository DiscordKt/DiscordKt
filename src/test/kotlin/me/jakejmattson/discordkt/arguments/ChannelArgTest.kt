package me.jakejmattson.discordkt.arguments

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optionalSnowflake
import dev.kord.core.cache.data.ChannelData
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import me.jakejmattson.discordkt.util.*

class ChannelArgTest : DescribeSpec({

    fun channelData(_guildId: Long = 0L) = mockk<ChannelData>(relaxed = true) {
        every { guildId } returns Snowflake(_guildId).optionalSnowflake()
    }

    describe("when the input is valid") {
        val arg = ChannelArg
        val inputChannel = TextChannel(channelData(), kord())

        transform(arg) {
            inputChannel becomes inputChannel
        }

        describe("when guild does not match but allowsGlobal") {
            val arg = ChannelArg<TextChannel>(allowsGlobal = true)
            val inputChannel = TextChannel(channelData(1234L), kord())
            val guild = guild(4321L.toSnowflake())

            transform(arg, discordContext(_guild = guild)) {
                inputChannel becomes inputChannel
            }
        }
    }

    describe("when the input is invalid") {

        // TODO This returns the VoiceChannel but I think it should be an error
        xdescribe("when the channel is the wrong type") {
            val arg = ChannelArg
            val inputChannel = VoiceChannel(channelData(), kord())

            transform(arg) {
                inputChannel producesError "Incorrect channel type"
            }
        }

        describe("when guild does not match and not allowsGlobal") {
            val arg = ChannelArg<TextChannel>(allowsGlobal = false)
            val inputChannel = TextChannel(channelData(1234L), kord())
            val guild = guild(4321L.toSnowflake())

            transform(arg, discordContext(_guild = guild)) {
                inputChannel producesError "Must be from this guild"
            }
        }
    }
})