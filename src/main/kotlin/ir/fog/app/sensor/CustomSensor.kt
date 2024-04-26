package ir.fog.app.sensor

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.entities.CustomTuple
import ir.fog.entities.TupleType
import ir.fog.placement.PlacementConfigs
import org.cloudbus.cloudsim.UtilizationModelFull
import org.fog.entities.Tuple
import org.fog.utils.FogUtils
import java.io.Serializable
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// add type to sensor Task base and data base
class CustomSensor(
    var id: String,
    var name: String?,
    var tupleType: String?,
    // implemented from cloud sim
    var userId: Int,
    // implemented from cloud sim
    var appId: String?,
    // Second
    private var frequency: Double = 1.0,
    // Second
    var delay: Double = 0.0,
    var config: PlacementConfigs,
    var selectivity: Double = 0.8,

    var isCloudTupleGenerator: Boolean = false,
    var tupleCpuLength:Long = 1000,
    var tupleNwLength:Long = 1000,

) : /*for converting to json and reverse*/Serializable {
    var gatewayId: String = ""
    private var lastTriggeredEvent: TupleType = TupleType.Task

    // for how many task then we should send data or task/data
    private var taskTriggeredCount = 5

    // implemented from ifogsim
    private var outputSize: Long = 10

    // for erasing the rx observable(preventing memory leaks)
    private var disposable = CompositeDisposable()



    fun startEntity() {
        disposable = CompositeDisposable()
        EventBus.observe()
            .filter { it.eventType == SimEvents.STOP_EXECUTION }
            .subscribe({
                stop()
            }) { println(it.message) }

        // for simulating cpu length and network length that we used in fog device. value of these fields fills by user.
        val cpuLength = tupleCpuLength
        val nwLength = tupleNwLength

        val tuple = CustomTuple(
            this.name + Random.nextInt(100000).toString(),
            appId,
            FogUtils.generateTupleId(),
            Tuple.UP,
            cpuLength,
            1,
            nwLength,
            outputSize,
            UtilizationModelFull(),
            UtilizationModelFull(),
            UtilizationModelFull(),
            40.0,
            TupleType.DATA,
            false,
            configs = config,
            isCloudTuple = isCloudTupleGenerator
        )
        tuple.userId = userId
        tuple.tupleType = tupleType
        tuple.expireTime = 2.0


        sendTuple(tuple)
    }

    private fun sendTuple(tuple: CustomTuple) {
        disposable.add(
            Observable.interval(0, (frequency * 1000).toLong(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .map {
                    /*we should check articles number of sending tasks in real world*/
                    tuple.runtimeID = "runtimeID" + Random.nextInt(100000).toString()
                    if (lastTriggeredEvent == TupleType.Task && taskTriggeredCount >= 5) {
                        tuple.type = TupleType.DATA
                        lastTriggeredEvent = TupleType.DATA
                        taskTriggeredCount = 0
                    } else if (lastTriggeredEvent == TupleType.DATA && taskTriggeredCount == 0) {
                        lastTriggeredEvent = TupleType.TASK_DATA
                        taskTriggeredCount = 0
                        tuple.type = TupleType.TASK_DATA
                    } else {
                        tuple.type = TupleType.Task
                        lastTriggeredEvent = TupleType.Task
                        taskTriggeredCount += 1
                    }
                    return@map FogEvent(
                        id,
                        0.0,
                        SimEvents.GATEWAY_TUPLE,
                        tuple,
                        currentEntityId = id,
                        nextEntity = gatewayId
                    )
                }
                .doOnNext {
                    if (Random.nextDouble(0.0, 1.0) < selectivity)
                        EventBus.sendEvent(it)
                }
                .subscribe {
//                    println("Sensor with Id $name triggered and event ${(it.data as CustomTuple).type}")
                }
        )


    }

    fun shutdownEntity() {
        stop()
    }

    private fun stop() {
        disposable.clear()
    }

}
