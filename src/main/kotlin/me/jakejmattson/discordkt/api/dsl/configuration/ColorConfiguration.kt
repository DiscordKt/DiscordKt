package me.jakejmattson.discordkt.api.dsl.configuration

import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import java.awt.Color

/**
 * @suppress Used in sample
 */
@ConfigurationDSL
data class ColorConfiguration(
    var successColor: Color = Color.GREEN,
    var failureColor: Color = Color.RED,
    var infoColor: Color = Color.BLUE
)