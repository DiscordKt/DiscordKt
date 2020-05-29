@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.stdlib

fun Char.isDigitOrPeriod() = this.isDigit() || this == '.'