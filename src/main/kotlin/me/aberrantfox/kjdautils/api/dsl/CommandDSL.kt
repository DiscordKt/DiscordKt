package me.aberrantfox.kjdautils.api.dsl

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.businessobjects.CommandData
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.CommandStruct
import me.aberrantfox.kjdautils.internal.di.DIService
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import java.lang.IllegalArgumentException

annotation class CommandSet(val category: String = "uncategorized")

data class DiscordContext(
    val stealthInvocation: Boolean,
    val discord: Discord,
    val message: Message,
    val author: User = message.author,
    val channel: MessageChannel = message.channel,
    val guild: Guild? = null)
{
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

data class CommandEvent<T>(
    val commandStruct: CommandStruct,
    val container: CommandsContainer,
    private val discordContext: DiscordContext
) {
    val stealthInvocation = discordContext.stealthInvocation
    val discord = discordContext.discord
    val author = discordContext.author
    val message = discordContext.message
    val channel = discordContext.channel
    val guild = discordContext.guild

    var args: T = args() as T

    fun respond(msg: String) = discordContext.respond(msg)
    fun respond(embed: MessageEmbed) = discordContext.respond(embed)
    fun respondTimed(msg: String, millis: Long = 5000) = discordContext.respondTimed(msg, millis)
    fun respondTimed(embed: MessageEmbed, millis: Long = 5000) = discordContext.respondTimed(embed, millis)
    fun unsafeRespond(msg: String) = discordContext.unsafeRespond(msg)
}

@CommandTagMarker
class Command(val name: String,
              var category: String = "",
              var expectedArgs: List<ArgumentType<*>> = listOf(),
              var requiresGuild: Boolean = false,
              var description: String = "No Description Provider") {

    operator fun invoke(args: Command.() -> Unit) {}

    val parameterCount: Int
        get() = this.expectedArgs.size

    fun<T : ArgumentContainer> execute(collection: ArgumentCollection<T>, event: (CommandEvent<T>) -> Unit) {

    }

    fun toCommandData(): CommandData {
        val expectedArgs = expectedArgs.joinToString {
            if (it.isOptional) "(${it.name})" else it.name
        }.takeIf { it.isNotEmpty() } ?: "<none>"

        return CommandData(name.replace("|", "\\|"),
            expectedArgs.replace("|", "\\|"),
            description.replace("|", "\\|"))
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

fun Command.execute(execute: (CommandEvent<*>) -> Unit) {
    execute(args(), execute)
}

fun<T> Command.execute(argument: ArgumentType<T>, execute: (CommandEvent<SingleArg<T>>) -> Unit) {
    execute(args(argument), execute)
}

fun<A, B> Command.execute(first: ArgumentType<A>, second: ArgumentType<B>, execute: (CommandEvent<DoubleArg<A, B>>) -> Unit) {
    execute(args(first, second), execute)
}

open class ArgumentContainer
class NoArg: ArgumentContainer()
data class SingleArg<T>(val first: T): ArgumentContainer()
data class DoubleArg<A, B>(val first: A, val second: B): ArgumentContainer()
data class TripleArg<A, B, C>(val first: A, val second: B, val third: C): ArgumentContainer()
data class QuadArg<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D): ArgumentContainer()

interface ArgumentCollection<T : ArgumentContainer> {
    val arguments: List<ArgumentType<*>>

    val size: Int
        get() = arguments.size

    fun bundle(arguments: List<ArgumentType<*>>): T
}

fun args() = object : ArgumentCollection<NoArg> {
    override val arguments: List<ArgumentType<*>>
        get() = listOf()

    override fun bundle(arguments: List<ArgumentType<*>>) = NoArg()
}

fun <T> args(first: ArgumentType<T>) = object : ArgumentCollection<SingleArg<T>> {
    override val arguments: List<ArgumentType<*>>
        get() = listOf(first)

    override fun bundle(arguments: List<ArgumentType<*>>) = SingleArg(arguments[0]) as SingleArg<T>
}

fun <A, B> args(first: ArgumentType<A>, second: ArgumentType<B>) = object : ArgumentCollection<DoubleArg<A, B>> {
    override val arguments: List<ArgumentType<*>>
        get() = listOf(first, second)

    override fun bundle(arguments: List<ArgumentType<*>>) = DoubleArg(arguments[0], arguments[1]) as DoubleArg<A, B>
}