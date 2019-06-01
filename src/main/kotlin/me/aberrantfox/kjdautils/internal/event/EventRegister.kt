package me.aberrantfox.kjdautils.internal.event

import com.google.common.eventbus.EventBus
import net.dv8tion.jda.core.events.*

object EventRegister {
    val eventBus = EventBus()
    fun onEvent(event: Event) = eventBus.post(event)
}
