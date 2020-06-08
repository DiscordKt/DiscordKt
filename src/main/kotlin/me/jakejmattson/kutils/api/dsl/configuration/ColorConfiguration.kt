package me.jakejmattson.kutils.api.dsl.configuration

import java.awt.Color

data class ColorConfiguration(
    var successColor: Color = Color.GREEN,
    var failureColor: Color = Color.RED,
    var infoColor: Color = Color.BLUE
)