package me.jakejmattson.discordkt.commands

import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.*
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.dsl.CommandException
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.internal.annotations.NestedDSL
import me.jakejmattson.discordkt.internal.command.parseArguments
import me.jakejmattson.discordkt.internal.command.transformArgs
import me.jakejmattson.discordkt.locale.inject

/**
 * The bundle of information to be executed when a command is invoked.
 *
 * @param arguments The [Argument]s accepted by this execution.
 * @param action The code to be run when this execution is fired.
 */
public data class Execution<T : CommandEvent<*>>(val arguments: List<Argument<*, *>>, val action: suspend T.() -> Unit) {
    /**
     * Mocks a method signature using [Argument] names, ex: (a, b, c)
     */
    val signature: String
        get() = "(${arguments.joinToString { it.name }})"

    /**
     * Each [Argument] separated by a space ex: a b c
     */
    val structure: String
        get() = arguments.joinToString(" ") {
            val type = it.name
            if (it.isOptional()) "[$type]" else type
        }

    /**
     * Run the command logic.
     */
    public suspend fun execute(event: T): Unit = action.invoke(event)
}

/**
 *
 * @property name The name of this command.
 * @property names All names (aliases) of this command.
 * @property description A brief description of the command - used in documentation.
 * @property requiredPermissions The permission level required to use this command.
 * @property category The category that this command belongs to - set automatically by CommandSet.
 * @property executions The list of [Execution] that this command can be run with.
 */
public sealed interface Command {
    public val name: String
    public val description: String
    public val category: String
    public val requiredPermissions: Permissions
    public val executions: MutableList<Execution<CommandEvent<*>>>

    public val names: List<String>
        get() = listOf(name)

    /**
     * Whether the command can parse the given arguments into a container.
     *
     * @param args The raw string arguments to be provided to the command.
     *
     * @return The result of the parsing operation.
     */
    public suspend fun canParse(context: DiscordContext, execution: Execution<*>, args: List<String>): Boolean = parseArguments(context, execution.arguments, args) is Success

    /**
     * Whether this command has permission to run with the given event.
     *
     * @param discord The event context that will attempt to run the command.
     */
    public suspend fun hasPermissionToRun(discord: Discord, author: User, guild: Guild?): Boolean = when {
        this is DmTextCommand && guild != null -> false
        this is GuildTextCommand && guild == null -> false
        else -> {
            if (guild != null)
                author.asMember(guild.id).getPermissions().contains(requiredPermissions)
            else
                false //TODO Handle global message commands
        }
    }

    /**
     * Invoke this command with the given args.
     */
    @OptIn(DelicateCoroutinesApi::class)
    public fun invoke(event: CommandEvent<TypeContainer>, args: List<String>) {
        GlobalScope.launch {
            val parseResults = executions.map { it to parseArguments(event.context, it.arguments, args) }
            val successfulParse = parseResults.firstOrNull { it.second is Success }

            if (successfulParse == null) {
                val failString = parseResults.joinToString("\n") {
                    val invocationExample =
                        if (parseResults.size > 1)
                            "${event.rawInputs.rawMessageContent.substringBefore(" ")} ${it.first.structure}\n"
                        else ""

                    invocationExample + (it.second as Error).error
                }

                event.respond(internalLocale.badArgs.inject(event.rawInputs.commandName) + "\n$failString")
                return@launch
            }

            val (execution, success) = successfulParse
            val transformedInput = transformArgs(execution.arguments.zip((success as Success).result), event.context)

            if (transformedInput is Success<*>)
                event.args = transformedInput.result as TypeContainer
            else {
                event.respond(transformedInput)
                return@launch
            }

            try {
                execution.execute(event)

                if (event.discord.configuration.deleteInvocation)
                    event.message?.delete()
            } catch (e: Exception) {
                event.discord.configuration.exceptionHandler.invoke(CommandException(e, event))
            }
        }
    }

    /**
     * Add an [Execution] to this [Command].
     * Called automatically by each execute block.
     * You should not need to call this manually.
     */
    public fun <T : CommandEvent<*>> addExecution(argTypes: List<Argument<*, *>>, execute: suspend T.() -> Unit) {
        executions.add(Execution(argTypes, execute) as Execution<CommandEvent<*>>)
    }
}

/**
 * Abstract text command representation.
 *
 * @property name The first provided name for a TextCommand.
 */
public sealed interface TextCommand : Command {
    override val name: String
        get() = names.first()
}

/**
 * Abstract slash command representation.
 *
 * @property execution The single execution of slash command.
 */
public sealed interface SlashCommand : Command {
    public val execution: Execution<CommandEvent<*>>
        get() = executions.first()
}

/**
 * A text command that can be executed from anywhere.
 */
public class GlobalTextCommand(override val names: List<String>,
                               override var description: String = "",
                               override val category: String,
                               override var requiredPermissions: Permissions,
                               override val executions: MutableList<Execution<CommandEvent<*>>> = mutableListOf()) : TextCommand {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend CommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public fun <A> execute(a: Argument<*, A>, execute: suspend CommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend CommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend CommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend CommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend CommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A text command that can only be executed in a guild.
 */
public class GuildTextCommand(override val names: List<String>,
                              override var description: String = "",
                              override val category: String,
                              override var requiredPermissions: Permissions,
                              override val executions: MutableList<Execution<CommandEvent<*>>> = mutableListOf()) : TextCommand {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend GuildCommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public fun <A> execute(a: Argument<*, A>, execute: suspend GuildCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend GuildCommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend GuildCommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend GuildCommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend GuildCommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A text command that can only be executed in a DM.
 */
public class DmTextCommand(override val names: List<String>,
                           override var description: String = "",
                           override val category: String,
                           override var requiredPermissions: Permissions,
                           override val executions: MutableList<Execution<CommandEvent<*>>> = mutableListOf()) : TextCommand {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend DmCommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public fun <A> execute(a: Argument<*, A>, execute: suspend DmCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend DmCommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend DmCommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend DmCommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend DmCommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A slash command that can be executed anywhere.
 *
 * @property name The name of the slash command.
 */
public class GlobalSlashCommand(override val name: String,
                                override val description: String,
                                override val category: String,
                                override val requiredPermissions: Permissions,
                                override val executions: MutableList<Execution<CommandEvent<*>>> = mutableListOf()) : SlashCommand {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend SlashCommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public fun <A> execute(a: Argument<*, A>, execute: suspend SlashCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend SlashCommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend SlashCommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend SlashCommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend SlashCommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A slash command that can be executed in a guild.
 *
 * @property name The name of the slash command.
 */
public open class GuildSlashCommand(override val name: String,
                                    override val description: String,
                                    override val category: String,
                                    override val requiredPermissions: Permissions,
                                    override val executions: MutableList<Execution<CommandEvent<*>>> = mutableListOf()) : SlashCommand {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend GuildSlashCommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public open fun <A> execute(a: Argument<*, A>, execute: suspend GuildSlashCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend GuildSlashCommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend GuildSlashCommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend GuildSlashCommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend GuildSlashCommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A command that can be executed via the context menu.
 *
 * @property displayText The text shown in the context menu.
 */
public class ContextCommand(override val name: String,
                            public val displayText: String,
                            override val description: String,
                            override val category: String,
                            override val requiredPermissions: Permissions,
                            override val executions: MutableList<Execution<CommandEvent<*>>> = mutableListOf()) : GuildSlashCommand(name, description, category, requiredPermissions, executions) {
    /** @suppress */
    @NestedDSL
    public override fun <A> execute(a: Argument<*, A>, execute: suspend GuildSlashCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)
}

/**
 * Find a command by its name (case-insensitive).
 */
public fun <T : Command> List<T>.findByName(name: String): T? =
    if (this is TextCommand)
        find { cmd -> cmd.names.any { it.equals(name, true) } }
    else
        find { it.name.equals(name, true) }