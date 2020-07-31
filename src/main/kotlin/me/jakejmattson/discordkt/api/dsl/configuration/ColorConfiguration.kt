package me.jakejmattson.discordkt.api.dsl.configuration

import me.jakejmattson.discordkt.api.annotations.BotConfigurationDSL
import java.awt.Color

/**
 * @suppress Used in sample
 */
@BotConfigurationDSL
data class ColorConfiguration(
    var successColor: Color = Color.GREEN,
    var failureColor: Color = Color.RED,
    var infoColor: Color = Color.BLUE
)