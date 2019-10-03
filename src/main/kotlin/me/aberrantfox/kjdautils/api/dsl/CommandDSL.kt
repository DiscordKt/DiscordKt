package me.aberrantfox.kjdautils.api.dsl

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.businessobjects.CommandData
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.CommandStruct
import me.aberrantfox.kjdautils.internal.di.DIService
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import java.lang.IllegalArgumentException


annotation class CommandSet(val category: String = "uncategorized")

data class CommandEvent(val commandStruct: CommandStruct,
                        val message: Message,
                        var args: List<Any?>,
                        val container: CommandsContainer,
                        val stealthInvocation: Boolean,
                        val discord: Discord,
                        val author: User = message.author,
                        val channel: MessageChannel = message.channel,
                        val guild: Guild? = null) {

    fun respond(msg: String) = unsafeRespond(msg.sanitiseMentions())

    fun respond(embed: MessageEmbed) = this.channel.sendMessage(embed).queue()

    fun respondTimed(msg: String, millis: Long = 5000) {
        if(millis < 0) {
            throw IllegalArgumentException("RespondTimed: Delay cannot be negative.")
        }

        this.channel.sendMessage(msg.sanitiseMentions()).queue {
            GlobalScope.launch {
                delay(millis)
                it.delete().queue()
            }
        }
    }

    fun respondTimed(embed: MessageEmbed, millis: Long = 5000) {
        if(millis < 0) {
            throw IllegalArgumentException("RespondTimed: Delay cannot be negative.")
        }

        this.channel.sendMessage(embed).queue {
            GlobalScope.launch {
                delay(millis)
                it.delete().queue()
            }
        }
    }

    fun unsafeRespond(msg: String) =
            if(msg.length > 2000){
                val toSend = msg.chunked(2000)
                toSend.forEach{ channel.sendMessage(it).queue() }
            } else{
                channel.sendMessage(msg).queue()
            }

}

@CommandTagMarker
class Command(val name: String,
              var category: String = "",
              var expectedArgs: Array<out CommandArgument> = arrayOf(),
              var execute: (CommandEvent) -> Unit = {},
              var requiresGuild: Boolean = false,
              var description: String = "No Description Provider") {

    operator fun invoke(args: Command.() -> Unit) {}

    val parameterCount: Int
        get() = this.expectedArgs.size

    fun requiresGuild(requiresGuild: Boolean) {
        this.requiresGuild = requiresGuild
    }

    fun execute(execute: (CommandEvent) -> Unit) {
        this.execute = execute
    }

    fun expect(vararg args: CommandArgument) {
        this.expectedArgs = args
    }

    fun expect(vararg args: ArgumentType) {
        val clone = Array(args.size) { arg(WordArg) }

        for (x in args.indices) {
            clone[x] = arg(args[x])
        }

        this.expectedArgs = clone
    }

    fun expect(args: Command.() -> Array<out CommandArgument>) {
        this.expectedArgs = args()
    }

    fun toCommandData(): CommandData {
        val expectedArgs = expectedArgs.joinToString {
            if (it.optional) "(${it.type.name})" else it.type.name
        }.takeIf { it.isNotEmpty() } ?: "<none>"

        return CommandData(name.replace("|", "\\|"),
            expectedArgs.replace("|", "\\|"),
            description.replace("|", "\\|"))
    }
}

data class CommandArgument(val type: ArgumentType, val optional: Boolean = false, val defaultValue: Any? = null) {
    override fun equals(other: Any?): Boolean {
        if(other == null) return false

        if(other !is CommandArgument) return false

        return other.type == this.type
    }
}

@CommandTagMarker
data class CommandsContainer(var commands: HashMap<String, Command> = HashMap()) {
    operator fun invoke(args: CommandsContainer.() -> Unit) {}

    fun listCommands() = this.commands.keys.toList()

    fun command(name: String, construct: Command.() -> Unit = {}): Command? {
        val command = Command(name)
        command.construct()
        this.commands.put(name, command)
        return command
    }

    fun join(vararg cmds: CommandsContainer): CommandsContainer {
        cmds.forEach {
            this.commands.putAll(it.commands)
        }

        return this
    }

    fun has(name: String) = this.commands.containsKey(name)

    operator fun get(name: String) = this.commands.values.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }
}

fun produceContainer(path: String, diService: DIService): CommandsContainer {
    val cmdMethods = Reflections(path, MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(CommandSet::class.java)
            .map { it to (it.annotations.first { it is CommandSet } as CommandSet).category }

    if(cmdMethods.isEmpty()) {
        println("KUtils: No command methods detected.")
        return CommandsContainer()
    } else {
        println("KUtils: ${cmdMethods.size} command methods detected.")
    }

    val container = cmdMethods
            .map { (method, cmdSetCategory) ->
                (diService.invokeReturningMethod(method) as CommandsContainer) to cmdSetCategory
            }
            .map { (container, cmdSetCategory) ->
                container.also {
                    it.commands.values
                            .filter { it.category == "" }
                            .forEach { it.category = cmdSetCategory }
                }
            }
            .reduce { a, b -> a.join(b) }

    val lowMap = container.commands.mapKeys { it.key.toLowerCase() } as HashMap<String, Command>
    container.commands = lowMap

    return container
}


@DslMarker
annotation class CommandTagMarker

fun commands(construct: CommandsContainer.() -> Unit): CommandsContainer {
    val commands = CommandsContainer()
    commands.construct()
    return commands
}

fun arg(type: ArgumentType, optional: Boolean = false, default: Any? = null) = CommandArgument(type, optional, default)

fun arg(type: ArgumentType, optional: Boolean = false, default: (CommandEvent) -> Any?) = CommandArgument(type, optional, default)