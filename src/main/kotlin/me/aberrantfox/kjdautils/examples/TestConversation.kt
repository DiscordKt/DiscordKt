package me.aberrantfox.kjdautils.examples

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.*

@Convo
fun testConversation() = conversation(name = "test-conversation") {
    val name = prompt(WordArg) {
        "Please enter your name."
    }

    val age = promptUntil(
        argumentType = IntegerArg,
        initialPrompt = { "Please enter your age." },
        until = { it > 0 },
        errorMessage = { "Age must be positive!" }
    )

    respond("Nice to meet you $name! $age is a great age.")
}