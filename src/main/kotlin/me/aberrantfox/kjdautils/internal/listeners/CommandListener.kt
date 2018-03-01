package me.aberrantfox.kjdautils.internal.listeners


import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.CommandRecommender
import me.aberrantfox.kjdautils.internal.command.convertAndQueue
import me.aberrantfox.kjdautils.internal.command.getCommandStruct
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.deleteIfExists
import me.aberrantfox.kjdautils.extensions.jda.descriptor
import me.aberrantfox.kjdautils.extensions.jda.isCommandInvocation
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter


internal class CommandListener(val config: KJDAConfiguration,
                               val container: CommandsContainer,
                               val jda: JDA,
                               var log: BotLogger,
                               val guild: Guild,
                               val preconditions: ArrayList<(CommandEvent) -> Boolean> = ArrayList()) : ListenerAdapter() {
    init {
        CommandRecommender.addAll(container.commands.keys.toList())
    }

    fun addPrecondition(condition: (CommandEvent) -> Boolean) = preconditions.add(condition)

    override fun onGuildMessageReceived(e: GuildMessageReceivedEvent) = handleInvocation(e.channel, e.message, e.author, true)

    override fun onPrivateMessageReceived(e: PrivateMessageReceivedEvent) = handleInvocation(e.channel, e.message, e.author, false)

    private fun handleInvocation(channel: MessageChannel, message: Message, author: User, invokedInGuild: Boolean) {
        if ( !(isUsableEvent(message, channel.id, author)) ) return

        val (commandName, actualArgs) = getCommandStruct(message.contentRaw, config)

        val command = container.get(commandName)

        when {
            command != null -> {
                invokeCommand(command, commandName, actualArgs, message, author, invokedInGuild)
                log.cmd("${author.descriptor()} -- invoked $commandName in ${channel.name}")
            }
            else -> {
                val recommended = CommandRecommender.recommendCommand(commandName)
                channel.sendMessage("I don't know what ${commandName.replace("@", "")} is, perhaps you meant $recommended?").queue()
            }
        }

        if (invokedInGuild) handleDelete(message, config.prefix)
    }

    private fun invokeCommand(command: Command, name: String, actual: List<String>, message: Message, author: User, invokedInGuild: Boolean) {
        val channel = message.channel

        if (!(argsMatch(actual, command, channel))) return

        val event = CommandEvent(config, jda, channel, author, message, guild, container, actual)
        val passesPreconditions = preconditions.all { it(event) }

        if(passesPreconditions) {
            convertAndQueue(actual, command.expectedArgs.toList(), this, event, invokedInGuild, command, config)
        }
    }

    fun executeEvent(command: Command, event: CommandEvent, invokedInGuild: Boolean) {
        if (command.parameterCount == 0) {
            command.execute(event)
            return
        }

        if (command.requiresGuild && !invokedInGuild) {
            event.respond("This command must be invoked in a guild channel, and not through PM")
        } else {
            command.execute(event)
        }
    }

    private fun isUsableEvent(message: Message, channel: String, author: User): Boolean {
        if (message.contentRaw.length > 1500) return false

        if (!(message.isCommandInvocation(config))) return false

        if (author.isBot) return false

        return true
    }

    private fun handleDelete(message: Message, prefix: String) =
        if (!message.contentRaw.startsWith(prefix + prefix)) {
            message.deleteIfExists()
        } else {
            Unit
        }

    private fun argsMatch(actual: List<String>, cmd: Command, channel: MessageChannel): Boolean {
        val optionalCount = cmd.expectedArgs.filter { it.optional }.size

        if (cmd.expectedArgs.contains(arg(ArgumentType.Sentence)) || cmd.expectedArgs.contains(arg(ArgumentType.Splitter))) {
            if (actual.size < cmd.expectedArgs.size - optionalCount) {
                channel.sendMessage("You didn't enter the minimum number of required arguments: ${cmd.expectedArgs.size - optionalCount}.").queue()
                return false
            }
        } else {
            if(!(actual.size >= (cmd.expectedArgs.size - optionalCount)
                    && (actual.size <= cmd.expectedArgs.size))) {
                if (!cmd.expectedArgs.contains(arg(ArgumentType.Manual))) {
                    channel.sendMessage("This command requires at least ${cmd.expectedArgs.size - optionalCount} and a maximum of ${cmd.expectedArgs.size} arguments.").queue()
                    return false
                }
            }
        }

        return true
    }
}
