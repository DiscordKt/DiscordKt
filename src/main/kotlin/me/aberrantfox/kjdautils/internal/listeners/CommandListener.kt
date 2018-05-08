package me.aberrantfox.kjdautils.internal.listeners


import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.deleteIfExists
import me.aberrantfox.kjdautils.extensions.jda.descriptor
import me.aberrantfox.kjdautils.extensions.jda.isCommandInvocation
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.core.JDA
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
                               val preconditions: ArrayList<(CommandEvent) -> Boolean> = ArrayList()) : ListenerAdapter() {

    fun addPrecondition(condition: (CommandEvent) -> Boolean) = preconditions.add(condition)

    override fun onGuildMessageReceived(e: GuildMessageReceivedEvent) {
        handleInvocation(e.channel, e.message, e.author, true)
    }

    override fun onPrivateMessageReceived(e: PrivateMessageReceivedEvent) {
        handleInvocation(e.channel, e.message, e.author, false)
    }

    private fun handleInvocation(channel: MessageChannel, message: Message, author: User, invokedInGuild: Boolean) =
            launch(CommonPool) {
                if (!(isUsableCommand(message, channel.id, author))) return@launch

                val (commandName, actualArgs) = cleanCommandMessage(message.contentRaw, config)
                val command = container[commandName]

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

        getArgCountError(actual, command)?.let {
            channel.sendMessage(it).queue()
            return
        }

        val event = CommandEvent(config, jda, channel, author, message, container, actual)
        val conversionResult = convertArguments(actual, command.expectedArgs.toList(), event)

        when(conversionResult) {
            is ConversionResult.Results -> event.args = conversionResult.results.requireNoNulls()
            is ConversionResult.Error -> {
                event.safeRespond(conversionResult.error)
                return
            }
        }

        executeCommand(command, event, invokedInGuild)
    }

    private fun executeCommand(command: Command, event: CommandEvent, invokedInGuild: Boolean) {
        if(isDoubleInvocation(event.message, event.config.prefix)) {
            event.message.addReaction("\uD83D\uDC40").queue()
        }

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

    private fun isUsableCommand(message: Message, channel: String, author: User): Boolean {
        if (message.contentRaw.length > 1500) return false

        if (!(message.isCommandInvocation(config))) return false

        if (author.isBot) return false

        return true
    }

    private fun handleDelete(message: Message, prefix: String) =
            if (!isDoubleInvocation(message, prefix)) {
                message.deleteIfExists()
            } else Unit

    private fun isDoubleInvocation(message: Message, prefix: String) = message.contentRaw.startsWith(prefix + prefix)
}
