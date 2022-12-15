package me.jakejmattson.discordkt.util

import dev.kord.rest.builder.interaction.*
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.commands.ContextCommand

internal fun BaseInputChatBuilder.mapArgs(command: Command) {
    command.execution.arguments.forEach { argument ->
        val name = argument.name.lowercase()
        val description = argument.description

        data class ArgumentData(val argument: Argument<*, *>, val isRequired: Boolean, val isAutocomplete: Boolean)

        val (arg, isRequired, isAuto) = if (argument is WrappedArgument<*, *, *, *>) {
            ArgumentData(
                argument.innerType,
                !argument.containsType<OptionalArg<*, *, *>>(),
                argument.containsType<AutocompleteArg<*, *>>()
            )
        } else
            ArgumentData(argument, isRequired = true, isAutocomplete = false)

        when (arg) {
            is ChoiceArg<*> -> string(name, description) {
                required = isRequired

                arg.choices.forEach {
                    choice(it.toString(), it.toString())
                }
            }

            is AttachmentArgument<*> -> attachment(name, description) { required = isRequired }
            is UserArgument<*> -> user(name, description) { required = isRequired }
            is RoleArgument<*> -> role(name, description) { required = isRequired }
            is ChannelArgument<*> -> channel(name, description) { required = isRequired }
            is BooleanArgument<*> -> boolean(name, description) { required = isRequired }
            is IntegerArgument<*> -> int(name, description) { required = isRequired; autocomplete = isAuto }
            is DoubleArgument<*> -> number(name, description) { required = isRequired; autocomplete = isAuto }
            else -> string(name, description) { required = isRequired; autocomplete = isAuto }
        }
    }
}

internal fun MultiApplicationCommandBuilder.register(command: Command) {
    if (command is ContextCommand<*>) {
        when (command.execution.arguments.first()) {
            is MessageArg -> message(command.displayText) { defaultMemberPermissions = command.requiredPermissions }
            is UserArg -> user(command.displayText) { defaultMemberPermissions = command.requiredPermissions }
            else -> {}
        }
    }

    input(command.name.lowercase(), command.description.ifBlank { "<No Description>" }) {
        mapArgs(command)
        defaultMemberPermissions = command.requiredPermissions
    }
}