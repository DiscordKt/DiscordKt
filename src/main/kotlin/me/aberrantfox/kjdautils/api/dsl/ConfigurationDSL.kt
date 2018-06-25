package me.aberrantfox.kjdautils.api.dsl

data class KJDAConfiguration(var token: String = "", var prefix: String = "+", var commandPath: String = "", var deleteOnInvocation: Boolean = true)