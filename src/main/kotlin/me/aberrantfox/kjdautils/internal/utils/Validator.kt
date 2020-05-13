package me.aberrantfox.kjdautils.internal.utils

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.api.dsl.command.CommandsContainer
import me.aberrantfox.kjdautils.internal.arguments.EitherArg

internal class Validator {
    companion object {
        fun validateCommandMeta(commandsContainer: CommandsContainer) {
            commandsContainer.commands.forEach { command ->
                val args = command.expectedArgs.arguments

                args.filterIsInstance<EitherArg<*, *>>().forEach {
                    if (it.left == it.right) {
                        val arg = it.left::class.toString().substringAfterLast(".").substringBefore("$")
                        InternalLogger.error("Detected EitherArg with identical args ($arg) in command: ${command.names.first()}")
                    }
                }
            }
        }

        fun validateReaction(config: KConfiguration) {
            val currentReaction = config.commandReaction ?: return
            val emojiRegex = "[^\\x00-\\x7F]+ *(?:[^\\x00-\\x7F]| )*".toRegex()
            val isValid = emojiRegex.matches(currentReaction)

            if (!isValid) {
                InternalLogger.error("Provided command reaction is not valid. Falling back to no-reaction mode.")
                config.commandReaction = null
            }
        }
    }
}