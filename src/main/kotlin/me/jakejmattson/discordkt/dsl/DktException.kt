package me.jakejmattson.discordkt.dsl

import dev.kord.core.event.Event
import me.jakejmattson.discordkt.commands.CommandEvent

/**
 * Shared interface for handling exceptions.
 *
 * @param exception The [Exception] thrown internally.
 */
public sealed class DktException<T : Exception>(public val exception: T)

/**
 * Contains an exception thrown from a command event.
 *
 * @param event The [CommandEvent] that threw this exception.
 */
public class CommandException<T : Exception>(exception: T, public val event: CommandEvent<*>) : DktException<T>(exception)

/**
 * Contains an exception thrown from a listener.
 *
 * @param event The [Event] that threw this exception.
 */
public class ListenerException<T : Exception>(exception: T, public val event: Event) : DktException<T>(exception)