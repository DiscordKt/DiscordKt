package me.aberrantfox.kjdautils.examples

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.*

@Convo
fun testConversation() = conversation(name = "test-conversation") {
    val name = promptFor(WordArg) {
        "Please enter your name."
    }

    val age = promptFor(IntegerArg) {
        "Please enter your age."
    }

    respond("Nice to meet you $name! $age is a great age.")
}