package com.fogsim.fog.broker

import com.fogsim.fog.EventEmitter
import com.fogsim.fog.LogManager
import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.EntityStatus
import com.fogsim.fog.core.models.EventType
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

class BrokerController(val broker: Broker) {
    fun start() {
        broker.status = EntityStatus.STARTED
        handleEvents()
    }

    fun stop() {
        broker.status = EntityStatus.STOPPED
    }

    private fun handleEvents() {
        broker.status = EntityStatus.RUNNING
        EventEmitter.event.filter { filterEvent(it) }.onEach {
            sendEvent(event(data = it.data, receiverId = broker.serverId))
        }.launchIn(AppCoroutineScope())
    }

    private fun sendEvent(event: MainEvent) {
        if (dataSizeInBytes(event.data.value).toLong() < broker.validSize.first() || dataSizeInBytes(event.data.value).toLong() > broker.validSize[1]) {
            sendCloud(event)
        } else {
            EventEmitter.emit(event)
        }

    }

    private fun sendCloud(event: MainEvent) {
       // println("Broker ${broker.id} send data to cloud: ${event.data}")
        LogManager.customLog(eventType = EventType.BROKER, message = "Broker ${broker.id} send data to cloud: ${event.data}", sender = broker.id)
    }

    private fun sendToOtherBroker(event: BrokerEvent) {
        println("Broker ${broker.id} send data to other broker: ${event.data}")
    }

    private fun filterEvent(event: MainEvent): Boolean {
        return event.receiverId == broker.id && broker.status == EntityStatus.RUNNING
    }

    private fun dataSizeInBytes(data: String): Int = data.length * Char.SIZE_BITS

    private fun event(
        id: String = broker.id,
        sederId: String = broker.id,
        receiverId: String = "",
        data: Data,
        date: Long = Date().time
    ): MainEvent {
        val mainEvent = MainEvent()
        mainEvent.id = id
        mainEvent.senderId = sederId
        mainEvent.receiverId = receiverId
        mainEvent.data = data
        mainEvent.date = date
        mainEvent.eventType = EventType.BROKER
        return mainEvent
    }

}
