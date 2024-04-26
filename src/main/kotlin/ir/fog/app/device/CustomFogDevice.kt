package ir.fog.app.device

import io.reactivex.rxjava3.disposables.CompositeDisposable
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.entities.CustomFogDeviceCharacteristics
import ir.fog.entities.CustomTuple
import ir.fog.placement.PlacementConfigs
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.Vm
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppModule
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


open class CustomFogDevice(
    id: String,
    name: String,
    var characteristics: CustomFogDeviceCharacteristics,
    var vmAllocationPolicy: VmAllocationPolicy,
    var storageList: MutableList<Storage>,
    var schedulingInterval: Double,
    var uploadBandwidth: Double,
    var downloadBandwidth: Double,
    var uplinkLatency: Double,
    var ratePerMips: Double,
    var idlePower: Double = 40.0,
    var busyPower: Double = 90.0,
    var emptySpace: Double = 0.0,
    var totalSpace: Double = 0.0,
    var tupleList: MutableList<CustomTuple> = ArrayList(),
    var queueCount: Int = 0,
    var taskB1: Double = 0.4,
    var taskB2: Double = 0.5,
    var taskB3: Double = 0.6,
    var b1: Double = 0.4,
    var b2: Double = 0.5,
    var b3: Double = 0.6,
    var b4: Double = 0.7,
    var b5: Double = 0.8,
    var currentAttendanceTime: Double = 9.0,
    var devicePresenceMiddle: Double = 10.0,
    var devicesPresenceVariance: Double = 5.0,
    var aEpsilon: Double = 0.4,
    var bEpsilon: Double = 0.4,
    var tEpsilon: Double = 0.4,
    var host: Host,
    var type: FogDeviceType = FogDeviceType.mobile,
    var deviceBusytime: Long = 0,
    var minCloudletFileSize: Int = 1000,
    var minStoragePercent: Double = 0.6,
    var minFlue: Double = 0.5,
    var minQueue: Int = 2,
    var minExpireTime: Int = 1,
    var distance:Double=1.0,
    var CCk:Double=0.0,
    var serverToDeviceBw:Double=0.0,


//    var taskB1: Double = 0.9,
//    var taskB2: Double = 0.8,
//    var taskB3: Double = 0.7,
//    var b1: Double = 0.9,
//    var b2: Double = 0.8,
//    var b3: Double = 0.7,
//    var b4: Double = 0.6,
//    var b5: Double = 0.5,


/// todo add selectivity to device for device for shotdown and add timer for start


) : Serializable, Device(name, id) {

    var serverId: String = ""
    var createdTime = Date()
    var totalCpuTime: Long = 0
    var isInProcess = false
    var queue: MutableList<CustomTuple> = mutableListOf()
    var tupleRunTimeMap: HashMap<String, HashMap<String, Long>> = hashMapOf()
    private var tupleIds = mutableListOf<String>()
    var lastAssignedTime: Date = Date()
    var idleTime: Long = 0L
    var lastCalcuatedTime = Date()
    var disposable = CompositeDisposable()
    var MTTF: Long = 0L
    var MTTR: Long = 0L
    var MTBF: Long = 0L
    var networkCost: Double = 0.0
    var uploadCost: Double = 0.0
    var downloadCost: Double = 0.0


    //  tuple heat
    // 12-10
    // 13-11
    //14-15

    var tupleConfigEpsilonMap: MutableMap<String, Double> = hashMapOf()

    var currentCost = 0L

    // d2a1:[0.1,0.005,0.06]
    override fun startEntity() {
        if (tupleIds.isNullOrEmpty()) {
            tupleIds = mutableListOf()
        }
        EventBus.observe()
            .filter {
                (it.eventType == SimEvents.DEVICE_COPY_TUPLE ||
                        it.eventType == SimEvents.UPDATE_TUPLE ||
                        it.eventType == SimEvents.DEVICE_DELETE_TUPLE ||
                        it.eventType == SimEvents.DEVICE_PROCESS_TUPLE ||
                        it.eventType == SimEvents.UPDATE_DEVICE_CONFIG ||
                        it.eventType == SimEvents.UPDATE_DEVICE_TASK_CONFIG ||
                        it.eventType == SimEvents.UPDATE_TUPLE_CONFIG ||
                        it.eventType == SimEvents.DELETE_DEVICE_TUPLE) &&
                        (it.entityId == this.name || this.tupleIds.contains(it.entityId))
            }
            .delay(500, TimeUnit.MILLISECONDS)
            .subscribe({
                when (it.eventType) {
                    SimEvents.UPDATE_TUPLE_CONFIG -> {
                        downloadCost += 100/downloadBandwidth
                        if (it.data != null) {
                            tupleList.forEachIndexed { index, t ->
                                if (it.entityId == t.ID) {
                                    tupleList[index].configs = ((it).data) as PlacementConfigs
                                    return@forEachIndexed
                                }
                            }
                        }
                    }
                    SimEvents.DEVICE_COPY_TUPLE -> {
//                        println("Tuple Received for copy to Fog ${this.name}")
                        val tuple = it.data as CustomTuple
                        this.emptySpace = emptySpace - tuple.cloudletFileSize
                        downloadCost += tuple.cloudletFileSize/downloadBandwidth
                        this.tupleList.add(tuple)
                        this.tupleIds = tupleList.map { it.ID }.toMutableList()
                        updateEnergyConsumption()
                    }
                    SimEvents.DEVICE_DELETE_TUPLE -> {
//                        println("Tuple Received for delte to Fog ${this.name}")

                        tupleList = tupleList.filter { t -> t.ID != (it.data as CustomTuple).ID }.toMutableList()
                        queue = queue.filter { t -> t.ID != (it.data as CustomTuple).ID }.toMutableList()
                        uploadCost += 100/uploadBandwidth
                        EventBus.sendEvent(
                            FogEvent(
                                name,
                                0.0,
                                SimEvents.UPDATE_DEVICES_STATE,
                                currentEntityId = id,
                                nextEntity = serverId,
                                data = this
                            )
                        )


                    }
                    SimEvents.UPDATE_TUPLE -> {
                        val tuple = it.data as CustomTuple
                        downloadCost += tuple.cloudletFileSize/downloadBandwidth
                        tupleList.forEachIndexed { index, t ->
                            if (tuple.ID == t.ID) {
                                tupleList[index] = tuple
                                return@forEachIndexed
                            }
                        }
                    }
                    SimEvents.DEVICE_PROCESS_TUPLE -> {
                        downloadCost += (it.data as CustomTuple).cloudletFileSize/downloadBandwidth
                        processTupleArrival((it.data as CustomTuple))
                    }
                    SimEvents.DELETE_DEVICE_TUPLE -> {
                        tupleList = tupleList.filter { t -> t.ID != (it.data as CustomTuple).ID }.toMutableList()
                        queue = queue.filter { t -> t.ID != (it.data as CustomTuple).ID }.toMutableList()
                        uploadCost += 100/uploadBandwidth
                        EventBus.sendEvent(
                            FogEvent(
                                name,
                                100.0,
                                SimEvents.UPDATE_DEVICES_STATE,
                                this,
                                currentEntityId = id,
                                nextEntity = serverId
                            )
                        )

                    }

                    SimEvents.UPDATE_DEVICE_CONFIG -> {
                        val configList = it.data as? List<Double>
                        if (!configList.isNullOrEmpty() && configList.size > 4) {
                            b1 = configList[0]
                            b2 = configList[1]
                            b3 = configList[2]
                            b4 = configList[3]
                            b5 = configList[4]
                        }
                        downloadCost += 100/downloadBandwidth
                        EventBus.sendEvent(
                            FogEvent(
                                name,
                                100.0,
                                SimEvents.UPDATE_DEVICES_STATE,
                                this,
                                currentEntityId = id,
                                nextEntity = serverId
                            )
                        )
                    }

                    SimEvents.UPDATE_DEVICE_TASK_CONFIG -> {
                        val configList = it.data as? List<Double>
                        if (!configList.isNullOrEmpty() && configList.size > 2) {
                            characteristics.b1 = configList[0]
                            characteristics.b2 = configList[1]
                            characteristics.b3 = configList[2]
                        }
                        EventBus.sendEvent(
                            FogEvent(
                                name,
                                100.0,
                                SimEvents.UPDATE_DEVICES_STATE,
                                this,
                                currentEntityId = id,
                                nextEntity = serverId
                            )
                        )
                    }
                    else -> {
//                        println("Tuple Received for process to Fog ${this.name}")

                    }


                }


            }) { obj: Throwable -> obj.printStackTrace() }
    }

    private fun processTupleArrival(customTuple: CustomTuple) {
        customTuple.enterDeviceTime = Date()

//        println("device => ${name} queue count is => ${queue.size} ")
        queue.add(customTuple)
        queueCount = queue.size
        EventBus.sendEvent(
            FogEvent(
                name,
                100.0,
                SimEvents.UPDATE_DEVICES_STATE,
                this,
                currentEntityId = id,
                nextEntity = serverId
            )
        )
        totalCpuTime += customTuple.cloudletLength

        println("device => $name queue count is => ${queue.size} ")
        processTask()


    }


    fun processTask() {
        if (!isInProcess) {
            if (queue.size > 0) {
                var currentTime = Date()
                isInProcess = true
                val t = Timer()
                t.schedule(
                    object : TimerTask() {
                        override fun run() {
                            if (queue.size > 0) {
                                val tuple = queue.last()
                                println("device => ${name} queue count is => ${queue.size} ")



                                if (queue.size > 0) {
                                    val time = (Date().time - queue.last().enterDeviceTime.time).toLong()
                                    EventBus.sendEvent(
                                        FogEvent(
                                            queue.last().ID,
                                            0.0,
                                            SimEvents.UPDATE_TUPLE_RUNTIME,
                                            time, currentEntityId = id, nextEntity = serverId
                                        )
                                    )
                                    queue.removeLast()
                                }

                                EventBus.sendEvent(
                                    FogEvent(
                                        name,
                                        0.0,
                                        SimEvents.UPDATE_DEVICES_STATE,
                                        this@CustomFogDevice, currentEntityId = id, nextEntity = serverId
                                    )
                                )

                                isInProcess = false
                                lastAssignedTime = Date()
                                if (queue.size > 0) {
                                    processTask()

                                } else {
                                    EventBus.sendEvent(
                                        FogEvent(
                                            name,
                                            0.0,
                                            SimEvents.CHECK_FOR_FINISH_TASK,
                                            currentEntityId = id,
                                            nextEntity = serverId
                                        )
                                    )
                                }

                                queueCount = queue.size
                                updateBusyTime(Date().time - currentTime.time)
                                updateEnergyConsumption()
                                t.cancel()
                            }

                        }
                    }, queue.last().cloudletLength

                )
            }
        }
    }

    fun getUpdatedTupleConfigs(tupleId: String): List<Double> {
        val customTuple = tupleList.first { tupleId == it.ID }


        if (customTuple.cloudletFileSize > minCloudletFileSize || customTuple.configs.a1 > 1) {
            customTuple.configs.a1 = reduceEpsilonFrom(customTuple.configs.a1, "a")
        } else {
            customTuple.configs.a1 = addEpsilonTo(customTuple.configs.a1, "a")
        }

        if (emptySpace / host.storage.toDouble() < minStoragePercent || customTuple.configs.a2 > 1) {
            customTuple.configs.a2 = reduceEpsilonFrom(customTuple.configs.a2, "a")
        } else {
            customTuple.configs.a2 = addEpsilonTo(customTuple.configs.a2, "a")
        }

        if ((characteristics.flu > minFlue && customTuple.configs.a3 > 0.1) || customTuple.configs.a3 > 1) {
            customTuple.configs.a3 = reduceEpsilonFrom(customTuple.configs.a3, "a")
        } else {
            customTuple.configs.a3 = addEpsilonTo(customTuple.configs.a3, "a")
        }

        if ((queue.size > minQueue) || customTuple.configs.a4 < 1) {
            customTuple.configs.a4 = addEpsilonTo(customTuple.configs.a4, "a")

        } else {
            customTuple.configs.a4 = reduceEpsilonFrom(customTuple.configs.a4, "a")
        }

        if ((customTuple.expireTime > minExpireTime) || customTuple.configs.a5 < 1) {
            customTuple.configs.a5 = addEpsilonTo(customTuple.configs.a5, "a")
        } else {
            customTuple.configs.a5 = reduceEpsilonFrom(customTuple.configs.a5, "a")

        }

        return listOf(
            customTuple.configs.a1,
            customTuple.configs.a2,
            customTuple.configs.a3,
            customTuple.configs.a4,
            customTuple.configs.a5
        )
    }

    fun getUpdatedDeviceConfigs(): List<Double> {

        val cTime = createdTime.time
        val currentTime = Date().time
        if (currentTime - cTime > 20000 && b1 < 1) {
            b1 = addEpsilonTo(b1, "b")
        } else if (b1 > 0.1) {
            b1 = reduceEpsilonFrom(b1, "b")
        }

        if (devicesPresenceVariance < 5 || b2 > 1) {
            this.b2 = reduceEpsilonFrom(this.b2, "b")
        } else {
            this.b2 = addEpsilonTo(this.b2, "b")
        }


        if (emptySpace / host.storage.toDouble() < 0.6 || b3 > 1) {
            this.b3 = reduceEpsilonFrom(this.b3, "b")
        } else {
            this.b3 = addEpsilonTo(this.b3, "b")
        }


        if (queue.size < 2 || b4 > 1) {
            this.b4 = reduceEpsilonFrom(this.b4, "b")
        } else {
            this.b4 = addEpsilonTo(this.b4, "b")
        }

        if (queue.size > 2 || b5 > 1) {
            this.b5 = reduceEpsilonFrom(this.b5, "b")
        } else {
            this.b5 = addEpsilonTo(this.b5, "b")
        }


        return mutableListOf(this.b1, this.b2, this.b3, this.b4, this.b5)
    }

    fun getUpdatedTaskConfigs(): List<Double> {
        if (totalCpuTime / 1000 < 0.6 && characteristics.b1 < 1) {
            characteristics.b1 = addEpsilonTo(characteristics.b1, "t")
        } else {
            b1 = reduceEpsilonFrom(b1, "t")
        }

        if (queue.size < 2 || characteristics.b2 > 1) {
            characteristics.b2 = reduceEpsilonFrom(characteristics.b2, "t")
        } else {
            characteristics.b2 = addEpsilonTo(this.b2, "t")
        }

        if (queue.size > 2 || characteristics.b3 > 1) {
            characteristics.b3 = reduceEpsilonFrom(characteristics.b3, "t")
        } else {
            characteristics.b3 = addEpsilonTo(characteristics.b3, "t")
        }

        return mutableListOf(characteristics.b1, characteristics.b2, characteristics.b3)
    }

    fun updateBusyTime(bTime: Long) {
        deviceBusytime += bTime
    }

    fun getDeviceEnergy(): Double {
        val newBusyPower = TimeUnit.MILLISECONDS.toSeconds(deviceBusytime) * busyPower
        val newIdlePower =
            TimeUnit.MILLISECONDS.toSeconds((Date().time - createdTime.time) - deviceBusytime) * idlePower
        return newBusyPower + newIdlePower
    }

    fun getDeviceDelayCost(): Double {
        return uploadCost + downloadCost
    }

    private fun updateEnergyConsumption() {
        var totalMipsAllocated = 0.0
        for (vm in host.getVmList<Vm>()) {
            val operator = vm as AppModule
            operator.updateVmProcessing(
                CloudSim.clock(), vmAllocationPolicy.getHost(operator).vmScheduler
                    .getAllocatedMipsForVm(operator)
            )
            totalMipsAllocated += host.getTotalAllocatedMipsForVm(vm)
        }

        val newCost = (TimeUnit.MILLISECONDS.toSeconds(Date().time - createdTime.time)) * host.totalMips

        currentCost = newCost

        EventBus.sendEvent(
            FogEvent(
                name,
                100.0,
                SimEvents.UPDATE_DEVICE_COST,
                newCost,
                currentEntityId = id,
                nextEntity = serverId
            )
        )


    }

    override fun toString(): String {
        return "CustomFogDevice(name=$name, characteristics=$characteristics, emptySpace=$emptySpace, tupleList=$tupleList, queueCount=$queueCount, taskB1=$taskB1, taskB2=$taskB2, taskB3=$taskB3, b1=$b1, b2=$b2, b3=$b3, b4=$b4, b5=$b5, currentAttendanceTime=$currentAttendanceTime, devicePresenceMiddle=$devicePresenceMiddle, devicesPresenceVariance=$devicesPresenceVariance)"
    }


    ///// 2,3,4,5,6,7,1,2,6,3,3,3,3,3,3,3,3,3,3,3,3,3,3 = 10-3

    private fun addEpsilonTo(a: Double, type: String): Double {
        return when (type) {
            "a" -> {
                a + aEpsilon
            }
            "b" -> {
                a + bEpsilon
            }
            else -> {
                a + tEpsilon
            }
        }

    }

    private fun reduceEpsilonFrom(a: Double, type: String): Double {
        return when (type) {
            "a" -> {
                aEpsilon /= 2
                if (aEpsilon >= a) {
                    aEpsilon = abs(a - 0.001)
                }
                a - aEpsilon

            }
            "b" -> {
                bEpsilon /= 2
                if (bEpsilon >= a) {
                    bEpsilon = abs(a - 0.001)
                }
                a - bEpsilon
            }
            else -> {
                tEpsilon /= 2
                if (tEpsilon >= a) {
                    tEpsilon = abs(a - 0.001)
                }
                a - tEpsilon
            }
        }


    }

}
