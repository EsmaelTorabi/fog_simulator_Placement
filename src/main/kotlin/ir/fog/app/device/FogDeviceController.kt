package ir.fog.app.device

import ir.fog.entities.CustomTuple
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author mohsen on 1/13/23
 */
class FogDeviceController {
    private val _state: MutableStateFlow<FogDeviceState> = MutableStateFlow(FogDeviceState.initial)
    val state: MutableStateFlow<FogDeviceState> = _state

    fun handleEvent(event: FogDeviceEvent) {
        when (event) {
            is FogDeviceEvent.Start -> start()
            is FogDeviceEvent.ShotDown -> shotDown()
            is FogDeviceEvent.Stop -> stop()
            is FogDeviceEvent.OnReciveTuple -> onReciveTuple(event.tuple)
        }
    }

    private fun start() {
        _state.value = _state.value.build {
            isStarted = true
        }
    }

    private fun onReciveTuple(tuple: CustomTuple) {
        _state.value = _state.value.build {

        }
    }

    private fun shotDown() {
        _state.value = _state.value.build {
            isFinished = true
        }

    }

    private fun stop() {
        _state.value = _state.value.build {
            isStopped = true
        }
    }
}
