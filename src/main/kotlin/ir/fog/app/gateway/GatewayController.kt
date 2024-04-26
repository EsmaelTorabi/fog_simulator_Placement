package ir.fog.app.gateway

import ir.fog.core.EventBus.stop
import ir.fog.entities.CustomTuple
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author mohsen on 1/13/23
 */
class GatewayController {
    private val _state:MutableStateFlow<GatewayState> = MutableStateFlow(GatewayState.initial)
    val state:MutableStateFlow<GatewayState> = _state

    fun handleEvent(event: GateWayEvent) {
        when (event) {
            is GateWayEvent.Start -> start()
            is GateWayEvent.ShotDown -> shotDown()
            is GateWayEvent.Stop -> stop()
            is GateWayEvent.SendTupleToBroker -> sendTupleToBroker(event.tuple)
            is GateWayEvent.SendTupleToActuator -> sendTupleToActuator(event.tuple)
            is GateWayEvent.OnReciveTuple -> onReciveTuple(event.tuple)
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

    private fun sendTupleToActuator(tuple: CustomTuple) {
        _state.value = _state.value.build {

        }
    }

    private fun sendTupleToBroker(tuple: CustomTuple) {
        _state.value = _state.value.build {

        }

    }

    private fun shotDown() {
        _state.value = _state.value.build {
            isFinished = true
        }

    }
}
