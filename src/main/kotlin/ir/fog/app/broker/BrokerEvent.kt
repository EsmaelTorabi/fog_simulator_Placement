package ir.fog.app.broker

import ir.fog.entities.CustomTuple

/**
 * @author mohsen on 1/13/23
 */
sealed class BrokerEvent {
    object Start : BrokerEvent()
    object ShotDown : BrokerEvent()
    object Stop : BrokerEvent()
    object Pause : BrokerEvent()
    data class OnReciveTuple(val tuple: CustomTuple) : BrokerEvent()

}
