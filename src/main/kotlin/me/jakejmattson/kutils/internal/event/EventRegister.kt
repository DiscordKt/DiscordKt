package me.jakejmattson.kutils.internal.event

import com.google.common.eventbus.EventBus
import net.dv8tion.jda.api.events.GenericEvent

internal object EventRegister {
    val eventBus = EventBus()
    fun onEvent(event: GenericEvent) = eventBus.post(event)
}
