package me.aberrantfox.kjdautils.internal.listeners


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.PreconditionResult
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.extensions.jda.deleteIfExists
import me.aberrantfox.kjdautils.extensions.jda.descriptor
import me.aberrantfox.kjdautils.extensions.jda.isCommandInvocation
import me.aberrantfox.kjdautils.extensions.jda.isDoubleInvocation
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.command.CommandExecutor
import me.aberrantfox.kjdautils.internal.command.CommandRecommender
import me.aberrantfox.kjdautils.internal.command.cleanCommandMessage
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent


internal class CommandListener(val config: KJDAConfiguration,
                               val container: CommandsContainer,
                               val jda: JDA,
                               var log: BotLogger,
                               private val executor: CommandExecutor) {

    @Subscribe fun guildMessageHandler(e: GuildMessageReceivedEvent) =
        handleMessage(e.channel, e.message, e.author, true)

    @Subscribe fun privateMessageHandler(e: PrivateMessageReceivedEvent) =
        handleMessage(e.channel, e.message, e.author, false)


    fun addPreconditions(vararg conditions: (CommandEvent) -> PreconditionResult) = executor.preconditions.addAll(conditions)

    private fun handleMessage(channel: MessageChannel, message: Message, author: User, invokedInGuild: Boolean) {

        if (!isUsableCommand(message, author)) return

        val (commandName, actualArgs) = cleanCommandMessage(message.contentRaw, config)

        val command = container[commandName]
        val isDoubleInvocation = message.isDoubleInvocation(config.prefix)

        if (command != null) {
            if (command.requiresGuild && !invokedInGuild) {
                channel.sendMessage("This command must be invoked in a guild channel and not through PM")
                return
            }

            executor.executeCommand(command, actualArgs, message)

            if (isDoubleInvocation) {
                message.addReaction("\uD83D\uDC40").queue()
            }

            log.cmd("${author.descriptor()} -- invoked $commandName in ${channel.name}")

        } else {
            val recommended = CommandRecommender.recommendCommand(commandName)
            channel.sendMessage("I don't know what ${commandName.sanitiseMentions()} is, perhaps you meant $recommended?").queue()
        }

        if (invokedInGuild && !isDoubleInvocation) message.deleteIfExists()
    }


    private fun isUsableCommand(message: Message, author: User): Boolean {
        if (message.contentRaw.length > 1500) return false

        if (!(message.isCommandInvocation(config))) return false

        if (author.isBot) return false

        return true
    }
}
