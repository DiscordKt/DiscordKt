package me.jakejmattson.discordkt.dsl

import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import java.util.*

internal fun uuid() = UUID.randomUUID().toString()

/**
 * @property label The prompt displayed above a text input field.
 * @property value A pre-filled value for this component, max 4000 characters.
 */
@BuilderDSL
public data class InputBuilder(val label: String,
                               var style: TextInputStyle = TextInputStyle.Paragraph,
                               var required: Boolean = true,
                               var allowedLength: ClosedRange<Int>? = null,
                               var value: String? = null,
                               var placeholder: String? = null)

@BuilderDSL
public class SimpleModalBuilder {
    internal val inputs: MutableList<InputBuilder> = mutableListOf()

    public fun input(label: String, builder: InputBuilder.() -> Unit) {
        val input = InputBuilder(label)
        input.builder()
        inputs.add(input)
    }

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

public suspend fun promptModal(interaction: ApplicationCommandInteraction, title: String, builder: SimpleModalBuilder.() -> Unit): Array<String?> {
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

    return retrieveValidModalResponse(modalId, inputIds.toList()).toTypedArray()
}

private fun retrieveValidModalResponse(modalId: String, textInputIds: List<String>): List<String?> = runBlocking {
    retrieveModalResponse(modalId, textInputIds) ?: retrieveValidModalResponse(modalId, textInputIds)
}

private suspend fun retrieveModalResponse(modalId: String, textInputIds: List<String>) = select<List<String?>?> {
    modalBuffer.onReceive { interaction ->
        if (interaction.modalId != modalId) return@onReceive null

        interaction.deferEphemeralResponse()

        textInputIds.map { id ->
            interaction.textInputs[id]?.value
        }
    }
}