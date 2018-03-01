package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*

data class CommandEvent(val config: KJDAConfiguration, val jda: JDA, val channel: MessageChannel,
                        val author: User, val message: Message, val guild: Guild, val container: CommandsContainer,
                        var args: List<Any> = listOf()) {

    fun respond(msg: String) =
        if(msg.length > 2000) {
            val toSend = msg.chunked(2000)
            toSend.forEach { channel.sendMessage(it).queue() }
        } else {
            this.channel.sendMessage(msg).queue()
        }

    fun respond(embed: MessageEmbed) = this.channel.sendMessage(embed).queue()

    fun safeRespond(msg: String) = respond(msg.sanitiseMentions())
}

@CommandTagMarker
class Command(var log: BotLogger, var expectedArgs: Array<out CommandArgument> = arrayOf(),
              var execute: (CommandEvent) -> Unit = {}, var requiresGuild: Boolean = false)  {
    val parameterCount = expectedArgs.size

    operator fun invoke(args: Command.() -> Unit) {}

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
        val clone = Array(args.size) { arg(ArgumentType.Word) }

        for (x in args.indices) {
            clone[x] = arg(args[x])
        }

        this.expectedArgs = clone
    }

    fun expect(args: Command.() -> Array<out CommandArgument>) {
        this.expectedArgs = args()
    }
}

data class CommandArgument(val type: ArgumentType, val optional: Boolean = false, val defaultValue: Any = "") {
    override fun equals(other: Any?): Boolean {
        if(other == null) return false

        if(other !is CommandArgument) return false

        return other.type == this.type
    }
}

@CommandTagMarker
data class CommandsContainer(var log: BotLogger, var commands: HashMap<String, Command> = HashMap()) {
    operator fun invoke(args: CommandsContainer.() -> Unit) {}

    fun listCommands() = this.commands.keys.toList()

    fun command(name: String, construct: Command.() -> Unit): Command? {
        val command = Command(log)
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

    fun get(name: String) = this.commands.get(name)

    fun newLogger(log: BotLogger) {
        this.log = log
        this.commands.values.forEach {
            it.log = log
        }
    }
}

@DslMarker
annotation class CommandTagMarker

fun commands(construct: CommandsContainer.() -> Unit): CommandsContainer {
    val commands = CommandsContainer(DefaultLogger())
    commands.construct()
    return commands
}

fun arg(type: ArgumentType, optional: Boolean = false, default: Any = "") = CommandArgument(type, optional, default)

fun arg(type: ArgumentType, optional: Boolean = false, default: (CommandEvent) -> Any) = CommandArgument(type, optional, default)