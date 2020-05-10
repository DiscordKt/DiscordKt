package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.*
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.command.Result.Error
import java.util.ArrayList

internal class CommandExecutor {
    fun executeCommand(command: Command, args: List<String>, event: CommandEvent<ArgumentContainer>) = runBlocking {
        GlobalScope.launch {
            invokeCommand(command, args, event)
        }
    }

    private fun invokeCommand(command: Command, actualArgs: List<String>, event: CommandEvent<ArgumentContainer>) {
        val shouldDeleteErrors = event.discord.configuration.deleteErrors

        getArgCountError(actualArgs, command)?.let {
            return if(shouldDeleteErrors) event.respondTimed(it) else event.respond(it)
        }

        val expected = command.expectedArgs.arguments
        val initialConversion = convertArguments(actualArgs, expected, event)

        if (initialConversion is Error) {
            val error = initialConversion.error

            if (!command.isFlexible || expected.size < 2) {
                return if(shouldDeleteErrors) event.respondTimed(error) else event.respond(error)
            }

            val permutations = generateAllPermutations(expected.toMutableList())

            val success = permutations
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
                .firstOrNull()
                ?: return if(shouldDeleteErrors) event.respondTimed(error) else event.respond(error)

            val orderedResult = expected.map { sortKey ->
                success.first {
                    it.first == sortKey
                }.second
            }

            val bundle = command.expectedArgs.bundle(orderedResult)
            command.invoke(bundle, event)

            return
        }

        val args = initialConversion as Result.Success
        val bundle = command.expectedArgs.bundle(args.results)

        command.invoke(bundle, event)
    }

    private fun <E> generateAllPermutations(original: MutableList<E>): List<List<E>> {
        if (original.isEmpty()) {
            val result: MutableList<List<E>> = ArrayList()
            result.add(ArrayList())
            return result
        }
        val firstElement: E = original.removeAt(0)
        val returnValue: MutableList<List<E>> = ArrayList()
        val permutations = generateAllPermutations(original)
        for (smallerPermutated in permutations) {
            for (index in 0..smallerPermutated.size) {
                val temp = ArrayList(smallerPermutated)
                temp.add(index, firstElement)
                returnValue.add(temp)
            }
        }

        return returnValue
    }
}
