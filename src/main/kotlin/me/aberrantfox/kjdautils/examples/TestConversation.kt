package me.aberrantfox.kjdautils.examples

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.*

@Convo
fun testConversation(config: MyCustomBotConfiguration) = conversation {
    name = "test-conversation"
    description = "Test conversation to test the implementation within KUtils."

    steps {
        promptFor(WordArg) {
            "Please enter your name."
        }

        promptFor(IntegerArg) {
            "Please enter your age."
        }
    }
}
