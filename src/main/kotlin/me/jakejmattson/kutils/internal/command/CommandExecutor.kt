package me.jakejmattson.kutils.internal.command

import kotlinx.coroutines.*
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.internal.arguments.*
import me.jakejmattson.kutils.internal.utils.InternalLogger
import java.util.ArrayList

internal class CommandExecutor {
    fun executeCommand(command: Command, args: List<String>, event: CommandEvent<GenericContainer>) = runBlocking {
        GlobalScope.launch {
            invokeCommand(command, args, event)
        }
    }

    private fun invokeCommand(command: Command, actualArgs: List<String>, event: CommandEvent<GenericContainer>) {
        val expected = command.arguments
        val initialConversion = convertArguments(actualArgs, expected, event)

        fun respondAccordingly(error: String) =
            if (event.discord.configuration.deleteErrors)
                event.respondTimed(error)
            else
                event.respond(error)

        val error = when (initialConversion) {
            is ConversionResult.Success -> return command.invoke(bundleToArgContainer(initialConversion.results), event)
            is ConversionResult.Error -> initialConversion.error
        }

        if (!command.isFlexible || expected.size < 2)
            return respondAccordingly(error)

        val successList = expected.toMutableList().generateAllPermutations()
            .mapNotNull {
                val conversion = convertArguments(actualArgs, it, event)

                if (conversion is ConversionResult.Success)
                    it to conversion.results
                else
                    null
            }
            .map { (argumentTypes, results) ->
                argumentTypes.zip(results)
            }

        if (successList.isEmpty())
            return respondAccordingly(error)

        if (successList.size > 1) {
            InternalLogger.error(
                """
                    Flexible command resolved ambiguously.
                    ${command.names.first()}(${expected.joinToString()})
                    Input: ${actualArgs.joinToString(" ")}
                """.trimIndent()
            )

            return respondAccordingly(error)
        }

        val success = successList.first()

        val orderedResult = expected.map { sortKey ->
            success.first {
                it.first == sortKey
            }.second
        }

        val bundle = bundleToArgContainer(orderedResult)
        command.invoke(bundle, event)
    }

    private fun <E> MutableList<E>.generateAllPermutations(): List<List<E>> {
        if (isEmpty())
            return listOf(listOf())

        val firstElement = removeAt(0)
        val returnValue: MutableList<List<E>> = ArrayList()

        generateAllPermutations().forEachIndexed { index, list ->
            val temp = list.toMutableList()
            temp.add(index, firstElement)
            returnValue.add(temp)
        }

        return returnValue
    }
}
