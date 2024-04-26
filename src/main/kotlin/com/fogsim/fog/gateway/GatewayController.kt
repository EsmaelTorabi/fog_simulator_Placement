package com.fogsim.fog.gateway

import com.fogsim.fog.EventEmitter
import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.EventType
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*


class GatewayController(val gateway: Gateway) {

    fun start() {
        handleEvents()
    }

    fun stop() {
    }

    private fun handleEvents() {
        EventEmitter.event.filter { it.receiverId == gateway.id }.onEach {
            sendEvent(event(data = it.data, receiverId = gateway.brokerId))
        }.launchIn(AppCoroutineScope())
    }

    private fun sendEvent(event: MainEvent) {
        EventEmitter.emit(event)
    }

    private fun event(
        id: String = gateway.id,
        sederId: String = gateway.id,
        receiverId: String = "",
        data: Data,
        date: Long = Date().time
    ):MainEvent {
        val mainEvent = MainEvent()
        mainEvent.id = id
        mainEvent.senderId = sederId
        mainEvent.receiverId = receiverId
        mainEvent.data = data
        mainEvent.date = date
        mainEvent.eventType = EventType.GATEWAY
        return mainEvent
    }
}