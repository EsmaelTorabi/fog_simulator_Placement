package ir.fog.app.sensor

import ir.fog.app.app.EventEmitter
import ir.fog.entities.CustomTuple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach

/**
 * @author mohsen on 1/13/23
 */
class SensorController(private val eventEmitter:EventEmitter) {
    private var _state: MutableStateFlow<SensorState> = MutableStateFlow(SensorState.initial)
    val state: MutableStateFlow<SensorState> = _state

    init {
        handleEvent()
    }

    private fun handleEvent() {
        eventEmitter.baseEvent.filter { it is SensorEvent }.onEach {
            when (val event = it as SensorEvent) {
                is SensorEvent.SendTuple -> sendTuple(event.tuple)
                is SensorEvent.Stop -> stop()
                is SensorEvent.ShotDown -> shotDown()
                SensorEvent.Resume -> TODO()
            }
        }


    }

    fun sendEvent(event: SensorEvent) {
        eventEmitter.emit(event)
    }


    private fun sendTuple(tuple: CustomTuple) {
        _state.value = _state.value.build {
            lastTriggeredEvent = tuple.type
        }
    }

    private fun stop() {
        _state.value = _state.value.build {
            isStopped = true
        }
    }

    private fun shotDown() {
        _state.value = _state.value.build {
            isFinished = true
        }
    }

}
