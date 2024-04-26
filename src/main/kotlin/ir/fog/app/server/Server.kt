package ir.fog.app.server

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import ir.fog.app.device.CustomFogDevice
import ir.fog.app.device.Device
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.Result
import ir.fog.core.SimEvents
import ir.fog.entities.*
import ir.fog.placement.DataPlacement
import ir.fog.placement.PlacementConfigs
import ir.fog.placement.TaskPlacement
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class Server(
    id: String,//
    name: String,//
    // implemented from ifogsim
    var characteristics: CustomFogDeviceCharacteristics,
    // implemented from ifogsim
    var vmAllocationPolicy: VmAllocationPolicy?,
    // implemented from ifogsim
    var storageList: MutableList<Storage>?,
    // implemented from ifogsim
    var schedulingInterval: Double,
    // upload bandwidth
    var uplinkBandwidth: Double,
    // download bandwidth
    var downlinkBandwidth: Double,
    // implemented from ifogsim
    var uplinkLatency: Double,
    // million instructioon per secoond implemented from ifogsim
    var ratePerMips: Double,

    var delay: Long = 200,
    // in second for duration of runtime adaption
    var runtimeDuration: Int = 2,
    var dataPlacementType: DataPlacementType = DataPlacementType.CUSTOM_CA_REPLICA,//
    var taskPlacementType: TaskPlacementType = TaskPlacementType.CUSTOM_PERFORMANCE_AWARE//

) : Device(name, id) {
    /// all fog devices
    var availableDevices: MutableList<CustomFogDevice> = mutableListOf()

    private var tupleToDeviceMap: HashMap<String, MutableList<CustomFogDevice>> = hashMapOf()

    var tupleList: MutableList<CustomTuple> = mutableListOf()

    @Transient
    private var garbageCollector: GarbageCollector = GarbageCollector(id)


    // todo implement separation of concern.
    @Transient
    private var monitor: Monitor = Monitor()



    @Transient
    private var runtimeAdaption: RuntimeAdaption = RuntimeAdaption(id)

    private var tupleConfigMap: HashMap<String, PlacementConfigs> = hashMapOf()
    // with this we know how much each tuple used network
    private var tupleNetworkMap: HashMap<String, MutableList<Long>> = hashMapOf()
    private var isSystemStopped = false

    @Transient
    private var disposable = CompositeDisposable()

    private var deviceEnergyMap: HashMap<String, Long> = hashMapOf()
    private var deviceTotalCost: HashMap<String, MutableList<Long>> = hashMapOf()
    // total time from entring the qeue to done
    private var tupleTimeMap: HashMap<String, MutableList<Long>> = hashMapOf()

    private var totalStorageCost = 0.0
    private var totalTransferCost = 0.0


    fun initializeVariables() {
        //
        runtimeDuration = 2
        tupleToDeviceMap = hashMapOf()
        tupleList = mutableListOf()
        tupleConfigMap = hashMapOf()
        tupleNetworkMap = hashMapOf()
        isSystemStopped = false

        deviceTotalCost = hashMapOf()
        tupleTimeMap = hashMapOf()
        deviceEnergyMap = hashMapOf()

        disposable = CompositeDisposable()
        garbageCollector = GarbageCollector(id)
        runtimeAdaption = RuntimeAdaption(id)

        monitor = Monitor()
        monitor.totalNetworkUsage = 0

    }


    override fun startEntity() {

        monitor.startLogging()
        if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA || dataPlacementType == DataPlacementType.CA_REPLICA)
            startRuntimeAdaption()
        handleGarbageCollector()
        EventBus.observe()
            .filter {
                (it.eventType == SimEvents.STOP_EXECUTION ||
                        it.eventType == SimEvents.SERVER_TUPLE ||
                        it.eventType == SimEvents.UPDATE_DEVICES_STATE ||
                        it.eventType == SimEvents.UPDATE_TUPLE_CONFIG ||
                        it.eventType == SimEvents.INCREASE_NUMBER_OF_TUPLE_COPIES ||
                        it.eventType == SimEvents.SHOT_DOWN_FOG_DEVICE ||
                        it.eventType == SimEvents.ADD_FOG_DEVICE ||
                        it.eventType == SimEvents.DECREASE_NUMBER_OF_TUPLE_COPIES ||
                        it.eventType == SimEvents.CHECK_FOR_FINISH_TASK ||
                        it.eventType == SimEvents.QUEUE_FINISHED ||
                        it.eventType == SimEvents.UPDATE_NETWORK_USAGE ||
                        it.eventType == SimEvents.UPDATE_ENERGY_CONSUMED ||
                        it.eventType == SimEvents.UPDATE_TUPLE_RUNTIME ||
                        it.eventType == SimEvents.UPDATE_DEVICE_COST ) /*&&  it.currentEntityId == id*/
            }
            .doOnNext { /*print("Server Event Received ${Date()} \n")*/ }
            .subscribe({ o: FogEvent ->
                when (o.eventType) {
                    SimEvents.UPDATE_DEVICES_STATE -> {
                        updateDeviceInLists(((o).data) as CustomFogDevice)

                    }
                    SimEvents.SERVER_TUPLE -> {
                        val tuple = ((o).data) as CustomTuple
                        handleReceivedTuple(tuple)
                    }
                    SimEvents.UPDATE_TUPLE_CONFIG -> {
                        tupleConfigMap[o.entityId] = ((o).data) as PlacementConfigs
                    }

                    SimEvents.INCREASE_NUMBER_OF_TUPLE_COPIES -> {
                        if (!tupleToDeviceMap[o.entityId].isNullOrEmpty()) {
                            val ids = tupleToDeviceMap[o.entityId]!!.map { it.name }

                            val newDeviceList = availableDevices.filter { !ids.contains(it.name) }

                            if (newDeviceList.isNotEmpty()) {
                                addNewDevicesToBestDevices(
                                    tupleList.first { t -> t.ID == o.entityId },
                                    newDeviceList,
                                    (o.data) as Int
                                )
                            }

                        }

                    }
                    SimEvents.DECREASE_NUMBER_OF_TUPLE_COPIES -> {
                        if (!tupleToDeviceMap[o.entityId].isNullOrEmpty()) {
                            val n = o.data as Int
                            val reduceCount =
                                if (n > tupleToDeviceMap[o.entityId]!!.size) tupleToDeviceMap[o.entityId]!!.size else n
                            for (i in 1..reduceCount) {
                                garbageCollector.deleteTupleFromDevice(
                                    tupleList.first { tu -> tu.ID == o.entityId },
                                    tupleToDeviceMap[o.entityId]!!.first()
                                )
                                tupleToDeviceMap[o.entityId]!!.removeFirst()
                            }
                        }
                    }

                    SimEvents.SHOT_DOWN_FOG_DEVICE -> {
                        val device = o.data as CustomFogDevice
                        availableDevices = availableDevices.filter { it.name == device.name }.toMutableList()

                        for (key in tupleToDeviceMap.keys) {
                            if (!tupleToDeviceMap[key].isNullOrEmpty())
                                tupleToDeviceMap[key] =
                                    tupleToDeviceMap[key]!!.filter { it.name == device.name }.toMutableList()
                        }
                    }

                    SimEvents.ADD_FOG_DEVICE -> {
                        val device = o.data as CustomFogDevice
                        availableDevices.add(device)
                    }
                    SimEvents.UPDATE_NETWORK_USAGE -> {
                        monitor.totalNetworkUsage += o.data as Long
                        monitor.updateNetworkUsageWeb("seerver")
                    }
                    SimEvents.UPDATE_ENERGY_CONSUMED -> {
                        if (o.data != null) {
                            if (deviceEnergyMap.isNullOrEmpty()) {
                                deviceEnergyMap = hashMapOf()
                            }
                            deviceEnergyMap[o.entityId] = o.data as Long
                        }

                    }
                    SimEvents.UPDATE_TUPLE_RUNTIME -> {
                        if (tupleTimeMap.isNullOrEmpty()) {
                            tupleTimeMap = hashMapOf()
                        }
                        if (o.data != null) {
                            if (tupleTimeMap[o.entityId].isNullOrEmpty()) {
                                tupleTimeMap[o.entityId] = mutableListOf()
                            }
                            if (tupleTimeMap[o.entityId] != null) {
                                tupleTimeMap[o.entityId]!!.add(o.data as Long)
                            }


                        }
//                        val list = mutableListOf<Long>()
//                        for (key in tupleTimeMap.keys) {
//                            list.add(tupleTimeMap[key]!!.sum() / tupleTimeMap[key]!!.size)
//                        }
//                        var av: Long = 0
//                        if (list.size > 0) {
//                            av = list.sum() / list.size
//                        }
//
//                        EventBus.sendEvent(
//                            FogEvent(
//                                entityId = "server",
//                                delay = 0.0,
//                                eventType = SimEvents.UPDATE_TUPLE_RUNTIME_WEB,
//                                currentEntityId = "seerver",
//                                nextEntity = "server",
//                                data = av
//                            )
//                        )


                    }

                    SimEvents.UPDATE_DEVICE_COST -> {
                        if (deviceTotalCost.isNullOrEmpty()) {
                            deviceTotalCost = hashMapOf()
                        }
                        if (o.data != null) {
                            if (deviceTotalCost[o.entityId] == null) {
                                deviceTotalCost[o.entityId] = mutableListOf()
                            }
                            deviceTotalCost[o.entityId]?.add(o.data as Long)
                        }
                    }

                    SimEvents.QUEUE_FINISHED -> {
                        funsystemStop();
                        println("End")
                    }
                    SimEvents.CHECK_FOR_FINISH_TASK -> {
                        var isFinished = false
                        for (key in tupleToDeviceMap.keys) {
                            var list = tupleToDeviceMap[key]!!.map { it.queue.size }
                            isFinished = list.sum() <= 0
                        }
                        if (isFinished) {
                            EventBus.sendEvent(
                                FogEvent(
                                    "server",
                                    500.0,
                                    SimEvents.QUEUE_FINISHED,
                                    currentEntityId = id,
                                    nextEntity = "",
                                    data = ""
                                )
                            )

                        }
                    }

                    SimEvents.STOP_EXECUTION -> {
                        println("SYSTEM STOPPED")
                        isSystemStopped = true
                        funsystemStop();
//                        stopServer()
                    }

                    else -> {}
                }

            })
            { obj: Throwable -> obj.printStackTrace() }

    }

    private fun funsystemStop() {
        if (isSystemStopped) {
            println("TOTAL Network Usage => ${monitor.totalNetworkUsage}")

            val list = mutableListOf<Long>()
            for (key in tupleTimeMap.keys) {
                list.add(tupleTimeMap[key]!!.sum() / tupleTimeMap[key]!!.size)
            }

            var av: Long = 0;
            if (list.size > 0) {
                av = list.sum() / list.size
            }
            println("AV Runtime => $av")

            var delayCost = 0.0

            for (device in availableDevices) {
                val cost = device.getDeviceDelayCost()
                delayCost += cost
            }

            println("TOTAL DelayCost => $delayCost")
            println("TOTAL Transfer => $totalTransferCost")
            println("TOTAL StorageCost => $totalStorageCost")

            println("New TOTAL Cost => ${ totalTransferCost + totalStorageCost}")


            var totalEnergy = 0.0
            for (device in availableDevices) {
                totalEnergy += device.getDeviceEnergy()
            }

            println("TOTAL Energy => $totalEnergy")

            val costList = mutableListOf<Long>()
            for (key in deviceTotalCost.keys) {
                costList.add(deviceTotalCost[key]!!.sum() / deviceTotalCost[key]!!.size)
            }
            println("AV Cost => ${costList.sum() / costList.size}")


            val result = Result()
            result.networkUsage =  monitor.totalNetworkUsage
            result.runtime = av
            result.energyConsumption = totalEnergy
            result.cost = costList.sum() / costList.size

            monitor.updateNetworkUsageWeb("seerver")

            EventBus.sendEvent(
                FogEvent(
                    entityId = "server",
                    delay = 0.0,
                    eventType = SimEvents.UPDATE_ENERGY_CONSUMED_WEB,
                    currentEntityId = "server",
                    nextEntity = "server",
                    data = totalEnergy
                )
            )

            EventBus.sendEvent(
                FogEvent(
                    entityId = "server",
                    delay = 0.0,
                    eventType = SimEvents.UPDATE_TUPLE_RUNTIME_WEB,
                    currentEntityId = "server",
                    nextEntity = "server",
                    data = av
                )
            )

            EventBus.sendEvent(
                FogEvent(
                    entityId = "server",
                    delay = 0.0,
                    eventType = SimEvents.UPDATE_DEVICE_COST_WEB,
                    currentEntityId = "server",
                    nextEntity = "server",
                    data = costList.sum() / costList.size
                )
            )

            EventBus.sendEvent(
                FogEvent(
                    id,
                    delay = 0.0,
                    SimEvents.SHOW_RESULTS,
                    data = result,
                    date = Date(),
                    currentEntityId = id,
                    nextEntity = id
                )
            )

            if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA || dataPlacementType == DataPlacementType.CA_REPLICA) {
                stopServer()
            }
        }

    }

    private fun updateDeviceInLists(device: CustomFogDevice) {
        availableDevices.forEachIndexed { index, d ->
            if (device.name == d.name) {
                availableDevices[index] = device
                return@forEachIndexed
            }
        }

        if (tupleToDeviceMap.isNotEmpty() && device.tupleList.isNotEmpty()) {
            for (t in device.tupleList) {
                if (tupleToDeviceMap[t.ID] != null) {
                    tupleToDeviceMap[t.ID]!!.forEachIndexed { index, d ->
                        if (d.name == device.name) {
                            tupleToDeviceMap[t.ID]!![index] = device
                            return@forEachIndexed
                        }
                    }

                } else {
                    break
                }

            }
        }
    }


    private fun handleReceivedTuple(tuple: CustomTuple) {
        if (tuple.type == TupleType.DATA) {
            if (tupleList.contains(tuple) || tupleToDeviceMap.containsKey(tuple.ID)) {
                updateDeviceTuple(tuple)
            } else {
                tupleList.add(tuple)
                handleDataPlacement(tuple)
            }
        } else if (tuple.type == TupleType.TASK_DATA) {
            if (tupleList.contains(tuple) || tupleToDeviceMap.containsKey(tuple.ID)) {
                updateDeviceTuple(tuple)
                handleTaskPlacement(tuple)
            } else {
                tupleList.add(tuple)
                if (taskPlacementType != TaskPlacementType.RANDOM) {
                    handleDataPlacement(tuple)
                }

                handleTaskPlacement(tuple)
            }
        } else {
            if (tupleList.contains(tuple) || tupleToDeviceMap.containsKey(tuple.ID)) {
                handleTaskPlacement(tuple)
            } else {

                monitor.updateNetworkUsage(id,tuple.cloudletFileSize)

                EventBus.sendEvent(
                    FogEvent(
                        "cloud",
                        0.0,
                        SimEvents.UPDATE_ENERGY_CONSUMED,
                        1000L,
                        currentEntityId = id,
                        nextEntity = "id"
                    )
                )
                EventBus.sendEvent(
                    FogEvent(
                        "cloud",
                        0.0,
                        SimEvents.CLOUD_TUPLE,
                        data = tuple,
                        currentEntityId = id,
                        nextEntity = ""
                    )
                )

            }
        }

    }

    private fun handleTaskPlacement(tuple: CustomTuple) {
        when (taskPlacementType) {
            TaskPlacementType.RANDOM -> {
                val n = Random.nextInt(0, availableDevices.size)
                sendTupleToFogDeviceForCopy(tuple, availableDevices[n])
                sendTupleToFogDeviceForProcess(tuple, availableDevices[n])
            }
            TaskPlacementType.DATA_AWARE -> {
                val n = Random.nextInt(0, tupleToDeviceMap[tuple.ID]!!.size)
                sendTupleToFogDeviceForProcess(tuple, tupleToDeviceMap[tuple.ID]!![n])
            }
            TaskPlacementType.PERFORMANCE_AWARE, TaskPlacementType.CUSTOM_PERFORMANCE_AWARE -> {
                val Up: MutableMap<String, Double> = HashMap()
                if (tupleToDeviceMap[tuple.ID] != null) {
                    for (device in tupleToDeviceMap[tuple.ID]!!) {
                        val characteristics = device.characteristics
                        val placement =
                            TaskPlacement(device, tuple, characteristics.b1, characteristics.b2, characteristics.b3)
                        Up[device.name] = placement.getDeviceAvailability()
                    }

                    if (Up.isNotEmpty()) {

                        val sortedMap = Up.toList().sortedBy { (_, v) -> v }.reversed().first()
                        val device = tupleToDeviceMap[tuple.ID]?.first { it.name == sortedMap.first }

                        if (taskPlacementType == TaskPlacementType.CUSTOM_PERFORMANCE_AWARE){
                            if (device != null) {
                                device.queueCount += 1
                                updateDeviceInLists(device);
                            }
                        }


                        monitor.tupleToDeviceMap.onNext(tupleToDeviceMap)
                        sendTupleToFogDeviceForProcess(tuple, device!!)
                    }

                }
            }
        }


    }

    private fun handleDataPlacement(tuple: CustomTuple) {
        if (tupleConfigMap.isNullOrEmpty()) {
            tupleConfigMap = hashMapOf();
        }
        if (tupleConfigMap[tuple.ID] != null) {
            tuple.configs = tupleConfigMap[tuple.ID]!!
        }

        when (dataPlacementType) {

            DataPlacementType.NO_REPLICA -> {

            }
            DataPlacementType.ONE_REPLICA -> {
                val n = Random.nextInt(0, availableDevices.size)
                tupleToDeviceMap[tuple.ID] = mutableListOf(availableDevices[n])

            }
            DataPlacementType.FULL_REPLICA -> {
                tupleToDeviceMap[tuple.ID] = availableDevices
            }
            DataPlacementType.CA_REPLICA -> {
                contextAwareDataOperation(tuple, false)
            }
            DataPlacementType.CUSTOM_CA_REPLICA -> {

                contextAwareDataOperation(tuple, true)

            }
        }

        if (tupleToDeviceMap[tuple.ID] != null) {
            for (device in tupleToDeviceMap[tuple.ID]!!) {
                sendTupleToFogDeviceForCopy(tuple, device)
            }
        }


        garbageCollector!!.addTask(tuple)

        monitor!!.tupleToDeviceMap.onNext(tupleToDeviceMap)

    }

    private fun contextAwareDataOperation(tuple: CustomTuple, isCustom: Boolean) {
        val placement = getDataPlacement(tuple, availableDevices)
        val numberOfCopies = if (isCustom) placement.getNumberOfCopies() else placement.getContextAwareNumberOfCopies()

        // println("Number of copies $numberOfCopies")
        val Up: MutableMap<String, Double> = getUP(tuple, availableDevices)
        val sortedMap = Up.toList().sortedBy { (k, v) -> v }.reversed().toMap()
        val bestDeviceListForCopy: MutableList<CustomFogDevice> = mutableListOf()
        val n = if (numberOfCopies < availableDevices.size) numberOfCopies else availableDevices.size
        for (i in 0 until n) {
            val device = availableDevices.first { it.name == sortedMap.keys.toList()[i] }
            bestDeviceListForCopy.add(device)
        }
        tupleToDeviceMap[tuple.ID] = bestDeviceListForCopy
    }

    private fun addNewDevicesToBestDevices(tuple: CustomTuple, deviceList: List<CustomFogDevice>, numberOfCopies: Int) {
        val Up: MutableMap<String, Double> = getUP(tuple, deviceList)
        val sortedMap = Up.toList().sortedBy { (k, v) -> v }.reversed().toMap()
        val bestDeviceListForCopy: MutableList<CustomFogDevice> = mutableListOf()
        val n = if (numberOfCopies < deviceList.size) numberOfCopies else deviceList.size
        for (i in 0 until n) {
            val device = deviceList.first { it.name == sortedMap.keys.toList()[i] }
            bestDeviceListForCopy.add(device)
        }



        for (device in bestDeviceListForCopy) {
            tupleToDeviceMap[tuple.ID]?.add(device)
            sendTupleToFogDeviceForCopy(tuple, device)
        }

    }

    private fun getUP(tuple: CustomTuple, deviceList: List<CustomFogDevice>): MutableMap<String, Double> {
        val placement = getDataPlacement(tuple, deviceList)

        val up: MutableMap<String, Double> = HashMap()
        for (device in deviceList) {
            up[device.name] = placement.getDataStorageLocation(
                device,
                device.b1,
                device.b2,
                device.b3,
                device.b4,
                device.b5,
                device.currentAttendanceTime,
                device.devicePresenceMiddle,
                device.devicesPresenceVariance
            )
        }
        return up
    }

    private fun getDataPlacement(tuple: CustomTuple, deviceList: List<CustomFogDevice>): DataPlacement {
        return DataPlacement(
            deviceList,
            tuple,
            tuple.configs.SD,
            tuple.configs.SMax,
            tuple.configs.a1,
            tuple.configs.a2,
            tuple.configs.a3,
            tuple.configs.a4,
            tuple.configs.a5,
            tuple.configs.FAva,
            tuple.configs.FPar,
            tuple.configs.normalizedCount
        )
    }

    private fun handleGarbageCollector() {
        this.garbageCollector.deadlineSubject
            .filter { true }
            .subscribe(
                { id ->

                    if (tupleToDeviceMap[id] != null) {

                        for (device in tupleToDeviceMap[id]!!) {
                            try {
                                sendDeleteTupleToFogDevice(tupleList.first { it.ID == id }, device)
                            } catch (e: Exception) {

                            }

                        }
                        tupleToDeviceMap.remove(id)
                    }
                    try {
                        tupleList.removeIf { it.ID == id }
                    } catch (e: Exception) {
                        println(e)
                    }


                    monitor!!.tupleToDeviceMap.onNext(tupleToDeviceMap)
                }
            ) { obj: Throwable -> obj.printStackTrace() }
    }


    private fun updateDeviceTuple(tuple: CustomTuple) {
        if (tupleToDeviceMap[tuple.ID] != null) {
            for (device in tupleToDeviceMap[tuple.ID]!!) {
                sendTupleToFogDeviceForUpdate(tuple, device)
            }
        }
    }

    private fun sendDeleteTupleToFogDevice(tuple: CustomTuple, device: CustomFogDevice) {
        EventBus.sendEvent(
            FogEvent(
                device.name,
                0.0,
                SimEvents.DEVICE_DELETE_TUPLE,
                tuple,
                currentEntityId = id,
                nextEntity = device.id
            )
        )

    }

    private fun sendTupleToFogDeviceForCopy(tuple: CustomTuple, device: CustomFogDevice) {
        if (tupleNetworkMap.isNullOrEmpty()) {
            tupleNetworkMap = hashMapOf()
        }
        if (tupleNetworkMap[tuple.ID].isNullOrEmpty()) {
            tupleNetworkMap[tuple.ID] = mutableListOf()
        }
        tupleNetworkMap[tuple.ID]!!.add(tuple.cloudletFileSize)

        monitor.updateNetworkUsage(id,tuple.cloudletFileSize)
        totalStorageCost += tuple.cloudletFileSize * device.distance
        totalTransferCost += tuple.cloudletFileSize / device.serverToDeviceBw * device.CCk
        EventBus.sendEvent(
            FogEvent(
                device.name,
                500.0,
                SimEvents.DEVICE_COPY_TUPLE,
                tuple,
                currentEntityId = id,
                nextEntity = device.id
            )
        )
    }

    private fun sendTupleToFogDeviceForUpdate(tuple: CustomTuple, device: CustomFogDevice) {
        if (tupleNetworkMap[tuple.ID].isNullOrEmpty()) {
            tupleNetworkMap[tuple.ID] = mutableListOf()
        }
        tupleNetworkMap[tuple.ID]!!.add(tuple.cloudletFileSize)

        monitor.updateNetworkUsage(id,tuple.cloudletFileSize)

        EventBus.sendEvent(
            FogEvent(
                device.name,
                500.0,
                SimEvents.UPDATE_TUPLE,
                tuple,
                currentEntityId = id,
                nextEntity = device.id
            )
        )
    }

    private fun sendTupleToFogDeviceForProcess(tuple: CustomTuple, device: CustomFogDevice) {
        EventBus.sendEvent(
            FogEvent(
                device.name,
                500.0,
                SimEvents.DEVICE_PROCESS_TUPLE,
                tuple,
                currentEntityId = id,
                nextEntity = device.id
            )
        )
    }

    private fun startRuntimeAdaption() {

        disposable.add(
            Observable.interval(0, runtimeDuration.toLong() * 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    if (!isSystemStopped) {
                        val keys = tupleToDeviceMap.keys
                        for (t in keys) {
                            val tuple = tupleList.first { it.ID == t }
                            runtimeAdaption.handleNewDataForUpdatingDeviceConfig(
                                tuple,
                                tupleToDeviceMap,
                                availableDevices,
                                taskPlacementType
                            )
                            runtimeAdaption.handleNewDataForNumberOfCopies(
                                tuple,
                                tupleToDeviceMap,
                               availableDevices /*tupleToDeviceMap[tuple.ID]!!*/,// todo check why we don't use all devices
                                dataPlacementType
                            )
                        }
                    }
                }) { obj: Throwable -> obj.printStackTrace() }
        )


    }


    fun stopServer() {
        garbageCollector!!.stop()
        monitor!!.stop()
        disposable.clear()
    }

}
