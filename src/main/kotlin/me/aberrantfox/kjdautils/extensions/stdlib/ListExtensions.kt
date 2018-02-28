package me.aberrantfox.kjdautils.extensions.stdlib

import java.security.SecureRandom


fun <T> ArrayList<T>.randomListItem() = this[randomInt(0, size - 1)]

fun randomInt(min: Int, max: Int): Int {
    val random = SecureRandom()
    return random.nextInt(max + 1 - min) + min
}