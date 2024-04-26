package com.fogsim.fog.sensor

import com.fogsim.fog.EventEmitter
import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.EventType
import com.fogsim.fog.core.utils.launchPeriodicAsync
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.random.Random

class SensorController(val sensor: Sensor) {



    private var job: Deferred<Unit>? = null

    fun start() {
        handleEvents()
        initTimer(sensor.frequency.toLong())
    }

    fun stop() {
        stopTimer()
    }

    private fun initTimer(period: Long) {
        job = AppCoroutineScope().launchPeriodicAsync(period) {
            val availability = Random.nextInt(0, 1)
            if (availability < sensor.availability) {
                sendEvent(
                    event(
                        data = Data(
                            id = sensor.id,
                            value = Random.nextInt(10, 8000).toString(2),
                            sensorType = sensor.sensorType,
                            config = sensor.dataConfig,
                            processTime = sensor.dataProcessTime
                        ),
                        receiverId = sensor.gatewayId
                    )
                )
            }

        }
    }

    private fun sendEvent(event: MainEvent) {
        EventEmitter.emit(event)
    }


    private fun stopTimer() {
        job?.cancel()
    }

    private fun handleEvents() {
        EventEmitter.event.filter { it.receiverId == sensor.id }.onEach {
            when (it) {
                is SensorEvent.SendData -> {
//                    val event = event(receiverId = sensor.gatewayId, data = it.data)
//                    sendEvent(event)
                }
            }
        }.launchIn(AppCoroutineScope())
    }

    private fun event(
        id: String = sensor.id,
        sederId: String = sensor.id,
        receiverId: String = "",
        data: Data = Data(sensor.id, Random.nextInt(0, 100).toString(2), sensor.sensorType),
        date: Long = Date().time
    ): MainEvent {
        val mainEvent = MainEvent()
        mainEvent.id = id
        mainEvent.senderId = sederId
        mainEvent.receiverId = receiverId
        mainEvent.data = data
        mainEvent.eventType = EventType.SENSOR
        return mainEvent
    }
}