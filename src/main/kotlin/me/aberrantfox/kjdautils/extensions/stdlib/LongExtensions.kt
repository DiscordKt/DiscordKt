package me.aberrantfox.kjdautils.extensions.stdlib


fun Long.convertToTimeString(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return "$days days, ${hours % 24} hours, ${minutes % 60} minutes, ${seconds % 60} seconds"
}