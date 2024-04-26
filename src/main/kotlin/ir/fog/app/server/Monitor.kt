package ir.fog.app.server

import io.reactivex.rxjava3.subjects.BehaviorSubject
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.app.device.CustomFogDevice

class Monitor() {
    var tupleToDeviceMap: BehaviorSubject<HashMap<String, MutableList<CustomFogDevice>>?> = BehaviorSubject.create()
    var totalNetworkUsage:Long = 0
// number of copy

    fun startLogging() {
        logEventBusEvents()
        tupleToDeviceMap
            .filter { true }
            .subscribe({
//                for (key in it!!.keys!!) {
////                    println("Tuple $key Mapped to devices ${it[key].toString()}")
//                }

            }) { obj: Throwable -> obj.printStackTrace() }
    }

    fun logEventBusEvents() {
        EventBus.observe()
            .filter { it.eventType == SimEvents.MONITOR_DELETE_TUPLE }
            .subscribe({
//                println("Tuple ${(it.data as CustomTuple).ID} Removed!!")
            }) { obj: Throwable -> obj.printStackTrace() }
    }

    fun  updateNetworkUsage(id:String,size:Long){
        EventBus.sendEvent(
            FogEvent(
                "server",
                500.0,
                SimEvents.UPDATE_NETWORK_USAGE,
                size,
                currentEntityId = id,
                nextEntity = ""
            )
        )
    }

    fun  updateNetworkUsageWeb(id:String){
        EventBus.sendEvent(
            FogEvent(
                entityId = "server",
                delay = 0.0,
                eventType = SimEvents.UPDATE_NETWORK_USAGE_WEB,
                currentEntityId = id,
                nextEntity = "server",
                data = totalNetworkUsage
            )
        )
    }


    fun stop() {
        tupleToDeviceMap.onComplete()
    }
}
