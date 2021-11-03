package me.jakejmattson.discordkt.dsl

import dev.kord.core.event.Event
import me.jakejmattson.discordkt.commands.CommandEvent

public sealed class DktException<T : Exception>(public val exception: T)

public class CommandException<T : Exception>(exception: T, public val event: CommandEvent<*>) : DktException<T>(exception)

public class ListenerException<T : Exception>(exception: T, public val event: Event) : DktException<T>(exception)