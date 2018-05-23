package me.aberrantfox.kjdautils.api.dsl

import com.sun.jdi.connect.Connector
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.Word
import me.aberrantfox.kjdautils.internal.di.DIService
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner


annotation class CommandSet

data class CommandEvent(val config: KJDAConfiguration, val jda: JDA, val channel: MessageChannel,
                        val author: User, val message: Message, val container: CommandsContainer,
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
open class Command(var log: BotLogger, open val name: String,  var expectedArgs: Array<out CommandArgument> = arrayOf(),
                   var execute: (CommandEvent) -> Unit = {}, var requiresGuild: Boolean = false) : BotLogger {
    override fun info(message: String) = log.info(message)
    override fun info(message: MessageEmbed) = log.info(message)

    override fun cmd(message: String) = log.cmd(message)
    override fun cmd(message: MessageEmbed) = log.cmd(message)

    override fun error(message: String) = log.error(message)
    override fun error(message: MessageEmbed) = log.error(message)

    override fun alert(message: String) = log.alert(message)
    override fun alert(message: MessageEmbed) = log.alert(message)

    override fun voice(message: String) = log.voice(message)
    override fun voice(message: MessageEmbed) = log.voice(message)

    override fun history(message: String) = log.history(message)
    override fun history(message: MessageEmbed) = log.history(message)

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
        val clone = Array(args.size) { arg(Word) }

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

    fun command(name: String, construct: Command.() -> Unit = {}): Command? {
        val command = Command(log, name)
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

    operator fun get(name: String) = this.commands.get(name)

    fun newLogger(log: BotLogger) {
        this.log = log
        this.commands.values.forEach {
            it.log = log
        }
    }
}

fun produceContainer(path: String, diService: DIService): CommandsContainer {
    val cmds = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(CommandSet::class.java)

    val container = cmds.map { diService.invokeReturningMethod(it) }
            .map { it as CommandsContainer }
            .reduce { a, b -> a.join(b) }

    val lowMap = HashMap<String, Command>()

    container.commands.keys.forEach {
        lowMap[it.toLowerCase()] = container.commands[it]!!
    }

    container.commands = lowMap

    return container
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