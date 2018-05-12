package me.aberrantfox.kjdautils.api.event

import com.google.common.eventbus.EventBus
import net.dv8tion.jda.core.events.*
import net.dv8tion.jda.core.hooks.EventListener


object EventRegister : EventListener {
    val eventBus = EventBus()
    override fun onEvent(event: Event) = eventBus.post(event)
}
