package ir.fog.app.sensor

import ir.fog.app.app.BaseEvent
import ir.fog.entities.CustomTuple

/**
 * @author mohsen on 1/13/23
 */
sealed class SensorEvent: BaseEvent(){
    data class SendTuple(val tuple: CustomTuple) : SensorEvent()
    object ShotDown : SensorEvent()
    object Stop : SensorEvent()
    object Resume: SensorEvent()
}
