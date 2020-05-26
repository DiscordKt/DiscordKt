package me.aberrantfox.kutils.internal.examples.implementations

import me.aberrantfox.kutils.api.annotations.CommandSet
import me.aberrantfox.kutils.api.arguments.*
import me.aberrantfox.kutils.api.dsl.command.commands
import me.aberrantfox.kutils.api.dsl.conversation.Conversation
import me.aberrantfox.kutils.api.services.*

//Conversations are a way to collect several pieces of data from a user without creating an unwieldy command.

class DemoConversation : Conversation() {
    @Start
    fun conversation() = me.aberrantfox.kutils.api.dsl.conversation.conversation(exitString = "exit") {
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

        //If you need access to previous messages, all ID's are stored in a list as the conversation progresses.
        println("Bot Messages: $botMessageIds -> $previousBotMessageId")
        println("User Messages: $userMessageIds -> $previousUserMessageId")

        //Conversations have access to every respond method, whether you're in a DM or a public channel
        respond("Nice to meet you $name! $age is a great age.")
    }
}

//The following commands start the above conversations in different contexts.
@CommandSet("Conversation Demo")
fun conversationCommands(conversationService: ConversationService) = commands {
    command("Private") {
        description = "Start a conversation with the user in DM's."
        execute(UserArg.makeOptional { it.author }) {
            val result = conversationService.startPrivateConversation<DemoConversation>(it.args.first)
            val response = evaluateConversationResult(result)
            it.respond(response)
        }
    }

    command("Public") {
        description = "Start a conversation with the user in this channel."
        execute(UserArg.makeOptional { it.author }) {
            val result = conversationService.startPublicConversation<DemoConversation>(it.args.first, it.channel)
            val response = evaluateConversationResult(result)
            it.respond(response)
        }
    }
}

private fun evaluateConversationResult(conversationResult: ConversationResult) =
    when (conversationResult) {
        ConversationResult.COMPLETE -> "Conversation Completed!"
        ConversationResult.EXITED -> "The conversation was exited by the user."
        ConversationResult.INVALID_USER -> "User must share a guild and cannot be a bot."
        ConversationResult.CANNOT_DM -> "User has DM's off or has blocked the bot."
        ConversationResult.HAS_CONVO -> "This user already has a conversation."
    }