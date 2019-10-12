package me.aberrantfox.kjdautils.examples

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.*
import java.awt.Color

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

    onComplete {
        val userName = responses.component1() as String
        val userAge = responses.component2() as Int

        val summary = embed {
            title = "Conversation Complete"
            description = "Here is a summary of what you told me!"
            color = Color.GREEN

            field {
                name = "Name"
                value = "You told me your name was: $userName"
            }
            field {
                name = "Age"
                value = "You told me your age was: $userAge"
            }
        }

        it.respond(summary)
    }
}
