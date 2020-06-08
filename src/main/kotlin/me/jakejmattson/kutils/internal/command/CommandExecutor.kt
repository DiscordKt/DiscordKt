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
        val shouldDeleteErrors = event.discord.configuration.deleteErrors
        val expected = command.arguments
        val initialConversion = convertArguments(actualArgs, expected, event)

        if (initialConversion is Result.Success) {
            val bundle = bundleToArgContainer(initialConversion.results)
            return command.invoke(bundle, event)
        }

        val error = (initialConversion as Result.Error).error

        if (!command.isFlexible || expected.size < 2)
            return if (shouldDeleteErrors) event.respondTimed(error) else event.respond(error)

        val successList = expected.toMutableList().generateAllPermutations()
            .mapNotNull {
                val conversion = convertArguments(actualArgs, it, event)

                if (conversion is Result.Success)
                    it to conversion.results
                else
                    null
            }
            .map { (argumentTypes, results) ->
                argumentTypes.zip(results)
            }

        if (successList.isEmpty())
            return if (shouldDeleteErrors) event.respondTimed(error) else event.respond(error)

        if (successList.size > 1) {
            InternalLogger.error(
                """
                    Flexible command resolved ambiguously.
                    ${command.names.first()}(${expected.joinToString()})
                    Input: ${actualArgs.joinToString(" ")}
                """.trimIndent()
            )

            return if (shouldDeleteErrors) event.respondTimed(error) else event.respond(error)
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
        if (isEmpty()) {
            val result: MutableList<List<E>> = ArrayList()
            result.add(ArrayList())
            return result
        }
        val firstElement = removeAt(0)
        val returnValue: MutableList<List<E>> = ArrayList()

        generateAllPermutations().forEachIndexed { index, list ->
            val temp = ArrayList(list)
            temp.add(index, firstElement)
            returnValue.add(temp)
        }

        return returnValue
    }
}
