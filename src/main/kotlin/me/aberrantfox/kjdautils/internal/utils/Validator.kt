package me.aberrantfox.kjdautils.internal.utils

import me.aberrantfox.kjdautils.api.dsl.command.CommandsContainer
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

internal class Validator {
    companion object {
        fun validateCommandConsumption(commandsContainer: CommandsContainer) {
            commandsContainer.commands.forEach { command ->
                val consumptionTypes = command.expectedArgs.arguments.map { it.consumptionType }

                if (!consumptionTypes.contains(ConsumptionType.All))
                    return@forEach

                val allIndex = consumptionTypes.indexOfFirst { it == ConsumptionType.All }
                val lastIndex = consumptionTypes.lastIndex

                if (allIndex == lastIndex)
                    return@forEach

                val remainingConsumptionTypes = consumptionTypes.subList(allIndex + 1, lastIndex + 1)

                remainingConsumptionTypes.takeWhile {
                    if (it != ConsumptionType.None) {
                        InternalLogger.error("Detected ConsumptionType.$it after ConsumptionType.All in command: ${command.names.first()}")
                        false
                    } else true
                }
            }
        }
    }
}