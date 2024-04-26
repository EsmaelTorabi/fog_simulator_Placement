package ir.fog.app.gateway

import ir.fog.app.sensor.CustomSensor
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.entities.CustomFogDeviceCharacteristics
import ir.fog.entities.CustomTuple
import ir.fog.app.device.Device
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.fog.entities.Actuator
import java.io.Serializable
import java.util.concurrent.TimeUnit

class Gateway(
    id: String,
    name: String,
    var characteristics: CustomFogDeviceCharacteristics?,
    var vmAllocationPolicy: VmAllocationPolicy?,
    var storageList: MutableList<Storage>?,
    var schedulingInterval: Double,
    var uplinkBandwidth: Double,
    var downlinkBandwidth: Double,
    var uplinkLatency: Double,
    var ratePerMips: Double,
    //milisecond
    var delay: Long = 0
) : Serializable, Device(name, id) {

     var brokerId: String = ""
    var sensorList: MutableList<CustomSensor> = mutableListOf()
    var actuatorList: MutableList<Actuator> = mutableListOf()

    init {

    }

    override fun startEntity() {
        handleReceivedTupleFromSensor()
    }

    private fun handleReceivedTupleFromSensor() {
        EventBus.observe()
            .filter { it.eventType == SimEvents.GATEWAY_TUPLE || it.eventType == SimEvents.ACTUATOR_TUPLE }
            .doOnNext { /*print("GateWay Event Received ${Date()} \n")*/ }
            .delay(delay, TimeUnit.MILLISECONDS)
            .subscribe({ o: Any? ->
                sendToBroker((o as FogEvent).data as CustomTuple)
            })
            { obj: Throwable -> obj.printStackTrace() }
    }

    fun handleReceivedTupleFromBroker(tuple: CustomTuple) {
    }


    private fun sendToBroker(tuple: CustomTuple) {
        EventBus.sendEvent(FogEvent(name, delay.toDouble(), SimEvents.BROKER_TUPLE, tuple,currentEntityId = id,nextEntity = brokerId))
    }

    fun sendToActuator(tuple: CustomTuple) {
        EventBus.sendEvent(FogEvent(this.name, delay.toDouble(), SimEvents.ACTUATOR_TUPLE, tuple))
    }


}
