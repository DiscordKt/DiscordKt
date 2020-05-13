package me.aberrantfox.kjdautils.examples.implementations

import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.services.*

//Conversations are a way to collect several pieces of data from a user without creating an unwieldy command.

class DemoConversation : Conversation() {
    @Start
    fun conversation() = conversation(exitString = "exit") {
        //This is a simple prompt that sends the user a message and waits for their response.
        //You will have access to the type-safe result immediately in this code for processing.
        //This will report an error if the argument parsing fails, and then send the prompt again.
        val name = blockingPrompt(AnyArg) {
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
        description = "Start a conversation with a user."
        execute(UserArg(allowsBot = true).makeOptional { it.author }) {
            val result = conversationService.startConversation<DemoConversation>(it.args.first)

            val response = when (result) {
                ConversationResult.COMPLETE -> "Conversation Completed!"
                ConversationResult.EXITED -> "The conversation was exited by the user."
                ConversationResult.INVALID_USER -> "User must share a guild and cannot be a bot."
                ConversationResult.CANNOT_DM -> "User has DM's off or has blocked the bot."
                ConversationResult.HAS_CONVO -> "This user already has a conversation."
            }

            it.respond(response)
        }
    }
}