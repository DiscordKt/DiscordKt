package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.arguments.WordArg
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.MessageEmbed

class Conversation(val name: String, val description: String,
                   val steps: List<Step>, var onComplete: (ConversationStateContainer) -> Unit = {})

data class Step(val prompt: Any, val expect: ArgumentType)

data class ConversationStateContainer(val userId: String, val guildId: String, var responses: MutableList<Any>,
                                      val conversation: Conversation, var currentStep: Int, val jda: JDA) {
    fun respond(message: String) = jda.getUserById(userId).sendPrivateMessage(message, DefaultLogger())
    fun respond(message: MessageEmbed) = jda.getUserById(userId).sendPrivateMessage(message, DefaultLogger())
}

fun conversation(block: ConversationBuilder.() -> Unit): Conversation = ConversationBuilder().apply(block).build()

class ConversationBuilder {
    var name = ""
    var description = ""
    private val steps = mutableListOf<Step>()
    private var onComplete: (ConversationStateContainer) -> Unit = {}

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

@Target(AnnotationTarget.FIELD)
annotation class Convo
