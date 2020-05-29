package me.jakejmattson.kutils.internal.examples.implementations

import com.google.common.eventbus.Subscribe
import me.jakejmattson.kutils.api.extensions.jda.fullName
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

//A listener is a function marked with @Subscribe that allows you to listen for specific discord events.
//These can be top-level, but are most commonly put into a class to allow for dependency injection.
class MessageLogger {
    @Subscribe
    fun onMessage(event: GuildMessageReceivedEvent) {
        println("${event.author.fullName()} :: ${event.message.contentRaw}")
    }
}