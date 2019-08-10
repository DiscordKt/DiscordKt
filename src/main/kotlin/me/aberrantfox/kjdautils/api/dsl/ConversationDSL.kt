package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import net.dv8tion.jda.api.entities.MessageEmbed

class Conversation(val name: String,
                   val description: String,
                   val steps: List<Step>,
                   var onComplete: (ConversationStateContainer) -> Unit = {}
)

data class Step(val prompt: Any, val expect: ArgumentType)

data class ConversationStateContainer(
    val userId: String,
    val guildId: String,
    var responses: MutableList<Any>,
    val conversation: Conversation,
    var currentStep: Int,
    val discord: Discord) {
    fun respond(message: String) = discord.getUserById(userId)?.sendPrivateMessage(message)
    fun respond(message: MessageEmbed) = discord.getUserById(userId)?.sendPrivateMessage(message)
}

fun conversation(block: ConversationBuilder.() -> Unit): Conversation = ConversationBuilder().apply(block).build()

class ConversationBuilder {
    var name = ""
    var description = ""
    val steps = mutableListOf<Step>()
    var onComplete: (ConversationStateContainer) -> Unit = {}

    fun steps(block: Steps.() -> Unit) {
        steps.addAll(Steps().apply(block))
    }

    fun onComplete(onComplete: (ConversationStateContainer) -> Unit) {
        this.onComplete = onComplete
    }

    fun build() = Conversation(name, description, steps, onComplete)
}

class Steps: ArrayList<Step>() {
    fun step(block: StepBuilder.() -> Unit) {
        add(StepBuilder().apply(block).build())
    }
}

class StepBuilder {
    var prompt: Any = ""
    var expect: ArgumentType = WordArg
    fun build(): Step = Step(prompt, expect)
}

@Target(AnnotationTarget.FUNCTION)
annotation class Convo
