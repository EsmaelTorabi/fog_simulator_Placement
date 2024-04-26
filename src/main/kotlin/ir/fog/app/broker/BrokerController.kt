package ir.fog.app.broker

import ir.fog.entities.CustomTuple
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author mohsen on 1/13/23
 */
class BrokerController {
    private var _state: MutableStateFlow<BrokerState> = MutableStateFlow(BrokerState.initial)
    val state: MutableStateFlow<BrokerState> = _state

    fun handleEvent(event: BrokerEvent) {
        when (event) {
            is BrokerEvent.Start -> start()
            is BrokerEvent.Stop -> stop()
            is BrokerEvent.ShotDown -> shotDown()
            is BrokerEvent.Pause -> pause()
            is BrokerEvent.OnReciveTuple -> onReciveTuple(event.tuple)
        }
    }

    private fun start() {
        _state.value = _state.value.build {
            isStarted = true
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

    private fun pause() {
        _state.value = _state.value.build {
            isPaused = true
        }
    }

    private fun onReciveTuple(tuple: CustomTuple) {
        _state.value = _state.value.build {

        }
    }
}
