package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.dsl.arguments.ArgumentType
import me.jakejmattson.discordkt.api.dsl.command.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

internal typealias Arg<T> = ArgumentType<T>

internal typealias execute0 = (CommandEvent<NoArgs>) -> Unit
internal typealias execute1<A> = (CommandEvent<Args1<A>>) -> Unit
internal typealias execute2<A, B> = (CommandEvent<Args2<A, B>>) -> Unit
internal typealias execute3<A, B, C> = (CommandEvent<Args3<A, B, C>>) -> Unit
internal typealias execute4<A, B, C, D> = (CommandEvent<Args4<A, B, C, D>>) -> Unit
internal typealias execute5<A, B, C, D, E> = (CommandEvent<Args5<A, B, C, D, E>>) -> Unit

internal typealias EmbedField = MessageEmbed.Field

internal typealias ReactionAction = (EmbedBuilder) -> Unit