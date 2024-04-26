package ir.fog.app.gateway

import ir.fog.entities.CustomTuple

/**
 * @author mohsen on 1/13/23
 */
sealed class GateWayEvent {
    object Start: GateWayEvent()
    object ShotDown : GateWayEvent()
    object Stop : GateWayEvent()
    data class SendTupleToBroker(val tuple: CustomTuple) : GateWayEvent()
    data class SendTupleToActuator(val tuple: CustomTuple) : GateWayEvent()
    data class OnReciveTuple(val tuple: CustomTuple) : GateWayEvent()
}
