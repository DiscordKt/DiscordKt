package me.jakejmattson.discordkt.prompts

import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.Args2
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.util.uuid

/**
 * @property label The prompt displayed above a text input field.
 * @property style The type of [TextInputStyle] to display as.
 * @property required Whether this field is required to be filled.
 * @property allowedLength A range of accepted input length.
 * @property value A pre-filled value, max 4000 characters
 * @property placeholder Placeholder while input is empty.
 */
@BuilderDSL
public data class InputBuilder(val label: String,
                               var style: TextInputStyle = TextInputStyle.Paragraph,
                               var required: Boolean = true,
                               var allowedLength: ClosedRange<Int>? = null,
                               var value: String? = null,
                               var placeholder: String? = null)

@BuilderDSL
/**
 * DSL for building a Discord modal.
 */
public class SimpleModalBuilder {
    internal val inputs: MutableList<InputBuilder> = mutableListOf()

    /**
     * Build a modal component using the [InputBuilder].
     *
     * @param label The label for this component.
     */
    public fun input(label: String, builder: InputBuilder.() -> Unit) {
        val input = InputBuilder(label)
        input.builder()
        inputs.add(input)
    }

    /**
     * Build a modal component without using the DSL.
     */
    public fun input(label: String,
                     style: TextInputStyle = TextInputStyle.Paragraph,
                     required: Boolean = true,
                     allowedLength: ClosedRange<Int>? = null,
                     value: String? = null,
                     placeholder: String? = null) {
        inputs.add(InputBuilder(label, style, required, allowedLength, value, placeholder))
    }
}

internal val modalBuffer = Channel<ModalSubmitInteraction>()

/**
 * Create a discord modal and collect the input.
 */
public suspend fun promptModal(interaction: ApplicationCommandInteraction, title: String, builder: SimpleModalBuilder.() -> Unit): Args2<DeferredEphemeralMessageInteractionResponseBehavior, Array<String?>> {
    val modalId = uuid()
    val modal = SimpleModalBuilder()
    modal.builder()

    val inputIds = mutableListOf<String>()

    interaction.modal(title, modalId) {
        modal.inputs.forEach { input ->
            actionRow {
                val inputId = uuid()

                textInput(input.style, inputId, input.label) {
                    this.required = input.required
                    this.value = input.value
                    this.placeholder = input.placeholder
                    this.allowedLength = input.allowedLength
                }

                inputIds.add(inputId)
            }
        }
    }

    return retrieveValidModalResponse(modalId, inputIds.toList())
}

private fun retrieveValidModalResponse(modalId: String, textInputIds: List<String>): Args2<DeferredEphemeralMessageInteractionResponseBehavior, Array<String?>> = runBlocking {
    retrieveModalResponse(modalId, textInputIds) ?: retrieveValidModalResponse(modalId, textInputIds)
}

private suspend fun retrieveModalResponse(modalId: String, textInputIds: List<String>): Args2<DeferredEphemeralMessageInteractionResponseBehavior, Array<String?>>? = select {
    modalBuffer.onReceive { interaction ->
        if (interaction.modalId != modalId) return@onReceive null
        Args2(interaction.deferEphemeralResponse(), textInputIds.map { id -> interaction.textInputs[id]?.value }.toTypedArray())
    }
}