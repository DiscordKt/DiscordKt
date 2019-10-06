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

data class CommandEvent<T: ArgumentContainer>(
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

    var args: T = NoArg() as T

    fun respond(msg: String) = discordContext.respond(msg)
    fun respond(embed: MessageEmbed) = discordContext.respond(embed)
    fun respondTimed(msg: String, millis: Long = 5000) = discordContext.respondTimed(msg, millis)
    fun respondTimed(embed: MessageEmbed, millis: Long = 5000) = discordContext.respondTimed(embed, millis)
    fun unsafeRespond(msg: String) = discordContext.unsafeRespond(msg)
}

@CommandTagMarker
class Command(val name: String,
              var category: String = "",
              var expectedArgs: ArgumentCollection<*> = args(),
              private var execute: (CommandEvent<*>) -> Unit = {},
              var requiresGuild: Boolean = false,
              var description: String = "No Description Provider") {

    fun invoke(parsedData: ArgumentContainer, event: CommandEvent<ArgumentContainer>) {
        event.args = parsedData
        execute.invoke(event)
    }

    val parameterCount: Int
        get() = this.expectedArgs.size

    fun<T : ArgumentContainer> execute(collection: ArgumentCollection<*>, event: (CommandEvent<T>) -> Unit) {
        expectedArgs = collection
        this.execute = event as (CommandEvent<*>) -> Unit
    }

    fun toCommandData(): CommandData {
        val expectedArgs = expectedArgs.arguments.joinToString {
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

    fun bundle(arguments: List<Any>): T
}

private fun args() =
    object : ArgumentCollection<NoArg> {
        override val arguments: List<ArgumentType<Nothing>>
            get() = listOf()

        override fun bundle(arguments: List<Any>) = NoArg()
    }

fun Command.execute(execute: (CommandEvent<NoArg>) -> Unit) {
    execute(args(), execute)
}

fun<T> Command.execute(argument: ArgumentType<T>, execute: (CommandEvent<SingleArg<T>>) -> Unit) {
    fun <A> args(first: ArgumentType<A>) =
        object : ArgumentCollection<SingleArg<A>> {
            override val arguments: List<ArgumentType<*>>
                get() = listOf(first)

            override fun bundle(arguments: List<Any>): SingleArg<A> {
                return SingleArg(arguments[0]) as SingleArg<A>
            }
        }

    execute(args(argument), execute)
}

fun<A, B> Command.execute(first: ArgumentType<A>, second: ArgumentType<B>, execute: (CommandEvent<DoubleArg<A, B>>) -> Unit) {
    fun <A, B> args(first: ArgumentType<A>, second: ArgumentType<B>) =
        object : ArgumentCollection<DoubleArg<A, B>> {
            override val arguments: List<ArgumentType<*>>
                get() = listOf(first, second)

            override fun bundle(arguments: List<Any>): DoubleArg<A, B> {
                return DoubleArg(arguments[0], arguments[1]) as DoubleArg<A, B>
            }
        }

    execute(args(first, second), execute)
}

fun<A, B, C> Command.execute(first: ArgumentType<A>, second: ArgumentType<B>, third: ArgumentType<C>, execute: (CommandEvent<TripleArg<A, B, C>>) -> Unit) {
    fun <A, B, C> args(first: ArgumentType<A>, second: ArgumentType<B>, third: ArgumentType<C>) =
        object : ArgumentCollection<TripleArg<A, B, C>> {
            override val arguments: List<ArgumentType<*>>
                get() = listOf(first, second, third)

            override fun bundle(arguments: List<Any>): TripleArg<A, B, C> {
                return TripleArg(arguments[0], arguments[1], arguments[2]) as TripleArg<A, B, C>
            }
        }

    execute(args(first, second, third), execute)
}

fun<A, B, C, D> Command.execute(first: ArgumentType<A>, second: ArgumentType<B>, third: ArgumentType<C>, fourth: ArgumentType<D>, execute: (CommandEvent<QuadArg<A, B, C, D>>) -> Unit) {
    fun <A, B, C, D> args(first: ArgumentType<A>, second: ArgumentType<B>, third: ArgumentType<C>, fourth: ArgumentType<D>) =
        object : ArgumentCollection<QuadArg<A, B, C, D>> {
            override val arguments: List<ArgumentType<*>>
                get() = listOf(first, second, third, fourth)

            override fun bundle(arguments: List<Any>): QuadArg<A, B, C, D> {
                return QuadArg(arguments[0], arguments[1], arguments[2], arguments[3]) as QuadArg<A, B, C, D>
            }
        }

    execute(args(first, second, third, fourth), execute)
}