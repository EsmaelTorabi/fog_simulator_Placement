package ir.fog.app.server

import io.reactivex.rxjava3.subjects.BehaviorSubject
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.app.device.CustomFogDevice
import ir.fog.entities.CustomTuple
import java.util.*
import java.util.concurrent.TimeUnit

class GarbageCollector(var serverId: String) {
    var deadlineSubject: BehaviorSubject<String?> = BehaviorSubject.create()

    init {
        EventBus.observe().filter {
            it.eventType == SimEvents.TUPLE_PROCESS_FINISHED
        }
            .delay(500, TimeUnit.MILLISECONDS)
            .subscribe({
                val tuple = it.data as CustomTuple
//                println("Task with Id of ${tuple.ID} is cleaned after Process")
                deadlineSubject.onNext(tuple.ID)
                EventBus.sendEvent(FogEvent("server", 500.0, SimEvents.MONITOR_DELETE_TUPLE, tuple,currentEntityId = serverId,nextEntity = serverId))
            }) { println(it) }
    }

    fun deleteTupleFromDevice(tuple: CustomTuple, device: CustomFogDevice) {
        EventBus.sendEvent(
            FogEvent(
                device.name,
                500.0,
                SimEvents.DELETE_DEVICE_TUPLE,
                tuple,
                currentEntityId = serverId,
                nextEntity = device.id
            )
        )
    }

    fun addTask(tuple: CustomTuple) {
        val t = Timer()
        t.schedule(
            object : TimerTask() {
                override fun run() {
//                    println("Task with Id of ${tuple.ID} is cleaned")
                    deadlineSubject.onNext(tuple.ID)
                    EventBus.sendEvent(
                        FogEvent(
                            "server",
                            500.0,
                            SimEvents.MONITOR_DELETE_TUPLE,
                            tuple,
                            currentEntityId = serverId,
                            nextEntity = serverId
                        )
                    )
                    t.cancel()
                }
            },
            (tuple.expireTime * 60 * 1000).toLong()
        )

    }

    fun stop() {
        deadlineSubject.onComplete()
    }
}
