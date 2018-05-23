package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.jda.obtainRole
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult.*
import net.dv8tion.jda.core.JDA

sealed class ArgumentResult {
    /** A result that has only consumed the single argument passed. **/
    data class Single(val result: Any) : ArgumentResult()

    /** A result that has consumed more than just the argument given. **/
    data class Multiple(val result: Any, val consumed: List<String>) : ArgumentResult()

    data class Error(val error: String) : ArgumentResult()
}

enum class ConsumptionType {
    Single, Multiple, All
}

interface ArgumentType {
    val consumptionType: ConsumptionType

    fun isValid(arg: String, event: CommandEvent): Boolean
    fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult
}

object IntegerArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = arg.isInteger()
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Single(arg.toInt())
}

object DoubleArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = arg.isDouble()
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Single(arg.toDouble())
}

object Choice : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = arg.isBooleanValue()
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Single(arg.toBooleanValue())
}

object URL : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = arg.containsURl()
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Single(arg)
}

object Word : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Single(arg)
}

object TimeString : ArgumentType {
    override val consumptionType = ConsumptionType.Multiple
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = convertTimeString(args)
}

object Sentence : ArgumentType {
    override val consumptionType = ConsumptionType.All
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Multiple(args.joinToString(" "), args)
}

object UserArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) { it.retrieveUserById(arg.trimToID()).complete() }

        return if (retrieved != null) {
            Single(retrieved)
        } else {
            Error("Couldn't retrieve user: $arg")
        }
    }
}

object Splitter : ArgumentType {
    override val consumptionType = ConsumptionType.All
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val joined = args.joinToString(" ")

        if (!joined.contains(separatorCharacter)) return Multiple(listOf(joined), args)

        return Multiple(joined.split(separatorCharacter).toList(), args)
    }
}

object TextChannelArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) { it.getTextChannelById(arg.trimToID()) }

        return if (retrieved != null) {
            Single(retrieved)
        } else {
            Error("Couldn't retrieve text channel: $arg")
        }
    }
}

object VoiceChannelArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) { it.getVoiceChannelById(arg.trimToID()) }

        return if (retrieved != null) {
            Single(retrieved)
        } else {
            Error("Couldn't retrieve user: $arg")
        }
    }
}

object RoleArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) { it.obtainRole(arg.trimToID()) }

        return if (retrieved != null) {
            Single(retrieved)
        } else {
            Error("Couldn't retrieve role: $arg")
        }
    }
}

object CommandArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = event.container.has(arg.toLowerCase())
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val command = event.container[arg.toLowerCase()]

        return if (command != null) {
            Single(command)
        } else {
            Error("Couldn't find command: $arg")
        }
    }
}

object MessageArg : ArgumentType {
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) {
            event.channel.getMessageById(arg.trimToID()).complete()
        }

        return if (retrieved != null) {
            Single(retrieved)
        } else {
            Error("Couldn't retrieve a message with the id given from this channel.")
        }
    }
}

object Manual : ArgumentType {
    override val consumptionType = ConsumptionType.All
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = Multiple(args, args)
}

fun tryRetrieveSnowflake(jda: JDA, action: (JDA) -> Any?): Any? =
        try {
            action(jda)
        } catch (e: RuntimeException) {
            null
        }