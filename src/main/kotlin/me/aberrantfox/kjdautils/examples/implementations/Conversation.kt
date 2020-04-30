package me.aberrantfox.kjdautils.examples.implementations

import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.services.ConversationService

//Conversations are a way to collect several pieces of data from a user without creating an unwieldy command.

class DemoConversation : Conversation() {
    @Start
    fun conversation() = conversation {
        //This is a simple prompt that sends the user a message and waits for their response.
        //You will have access to the type-safe result immediately in this code for processing.
        //This will report an error if the argument parsing fails, and then send the prompt again.
        val name = blockingPrompt(WordArg) {
            "Please enter your name."
        }

        //This is a slightly more complicated prompt that allows you to validate the input beyond the argument parsing.
        //You have access to the result and the ability to set your custom response when this check fails.
        val age = blockingPromptUntil(
            argumentType = IntegerArg,
            initialPrompt = { "Please enter your age." },
            until = { it > 0 },
            errorMessage = { "Age must be positive!" }
        )

        respond("Nice to meet you $name! $age is a great age.")
    }
}

@CommandSet("Conversation Demo")
fun conversationCommands(conversationService: ConversationService) = commands {
    //This command starts the above conversation
    command("Conversation") {
        description = "Start an example conversation."
        execute {
            conversationService.startConversation<DemoConversation>(it.author)
        }
    }
}