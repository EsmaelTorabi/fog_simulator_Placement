package com.fogsim.fog.server


import com.fogsim.fog.Application
import com.fogsim.fog.EventEmitter
import com.fogsim.fog.core.models.*
import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.fogDevice.FogDeviceEvent
import com.fogsim.fog.server.placement.DataPlacement
import com.fogsim.fog.server.placement.TaskPlacement
import kotlinx.coroutines.*
import kotlin.random.Random

class ServerDeviceHandler(val serverId: String, private val serverLogger: ServerLogger) {
    val taskList = mutableListOf<Data>()
    var isBusy = false
    var dataDeviceListMap: HashMap<String, MutableList<FogDevice>> = hashMapOf()
    private var analyzedDataDeviceListMap: HashMap<String, MutableList<FogDevice>> = hashMapOf()
    fun handleRawData(data: Data, deviceList: List<FogDevice>) {
        if (!dataDeviceListMap.containsKey(data.id)) {
            findBestDevicesForCopy(
                data,
                deviceList.filter {
                    it.status == EntityStatus.RUNNING && it.type in listOf(
                        FogDeviceType.Storage,
                        FogDeviceType.Analyzer,
                        FogDeviceType.All
                    )
                })
        }
        taskList.add(data)
        if (!isBusy) {
            isBusy = true
            bestDeviceForDoingTask()
        }


    }

    fun handleAnalyzingData(data: AnalyzedData, device: FogDevice) {
        if (!analyzedDataDeviceListMap.containsKey(data.id)) {
            analyzedDataDeviceListMap[data.id] = mutableListOf(device)
        } else {
            if (analyzedDataDeviceListMap[data.id] == null) {
                analyzedDataDeviceListMap[data.id] = mutableListOf(device)
            }
            if (analyzedDataDeviceListMap[data.id]!!.contains(device)) {
                analyzedDataDeviceListMap[data.id]!!.remove(device)
            }
            analyzedDataDeviceListMap[data.id]?.add(device)
        }
    }

    fun updateDevice(device: FogDevice) {
        for (id in dataDeviceListMap.keys) {
            val deviceList = dataDeviceListMap[id]
            if (deviceList != null) {
                if (deviceList.contains(device)) {
                    deviceList.remove(device)
                    deviceList.add(device)
                }
            }
        }
    }

    fun getProviderDeviceId(idList: List<String>): String? {
        for (id in idList) {
            if (analyzedDataDeviceListMap.containsKey(id)) {
                return analyzedDataDeviceListMap[id]?.first()?.id
            }
        }
        return null
    }


    private fun bestDeviceForDoingTask() {
        if (taskList.isEmpty()) {
            isBusy = false
            return
        }
        val data = taskList.first()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            delay(300)
            val up: MutableMap<String, Double> = HashMap()
            if (dataDeviceListMap[data.id] != null) {
                for (device in dataDeviceListMap[data.id]!!) {
                    if (device.type != FogDeviceType.Storage && device.type != FogDeviceType.Warner) {
                        val placement =
                            TaskPlacement(device)
                        up[device.id] = placement.getDeviceAvailability()
                    }


                }
                if (up.isNotEmpty()) {

                    val sortedMap = up.toList().sortedBy { (_, v) -> v }.reversed().first()
                    val device = dataDeviceListMap[data.id]?.first { it.id == sortedMap.first }
                    if (device != null) {
                        device.controller!!.queueCount += 1
                        updateDevice(device)
                    }
                    val e = FogDeviceEvent.Analyze(dataId = data.id)
                    e.senderId = serverId
                    e.eventType = EventType.SERVER
                    e.receiverId = device!!.id
                    EventEmitter.emit(e)
                    serverLogger.increaseDeviceTaskCounter(device.id)
                }

            }
            taskList.removeFirst()

            if (taskList.isNotEmpty())
                bestDeviceForDoingTask()
            else
                isBusy = false
        }


    }


    private fun findBestDevicesForCopy(data: Data, deviceList: List<FogDevice>) {
        val bestDeviceListForCopy = mutableListOf<FogDevice>()
        val numberOfCopies = getNumberOfCopies(data, deviceList)
        val up = getUP(data, deviceList)
        val sortedMap = up.toList().sortedBy { (_, v) -> v }.reversed().toMap()

        val n = if (numberOfCopies < deviceList.size) numberOfCopies else deviceList.size
        for (i in 0 until n) {
            val random = Random.nextDouble(0.0, 1.0)
            val device = deviceList.firstOrNull { it.id == sortedMap.keys.toList()[i] }
            if (device != null)
            {
                device.controller!!.resourceLogger.increaseDataCount()
                if(random < device.availability) {
                    device.controller!!.resourceLogger.increaseAcceptedDataCount()
                    bestDeviceListForCopy.add(device)
                }else{
                    device.controller!!.resourceLogger.increaseRejectedDataCount()
                }

            }
        }

        dataDeviceListMap[data.id] = bestDeviceListForCopy
    }

    private fun getNumberOfCopies(data: Data, deviceList: List<FogDevice>): Int {
        val dataPlacement = DataPlacement(deviceList, data)
        return when (Application.dataPlacementType) {
            DataPlacementType.NO_REPLICA -> 0
            DataPlacementType.ONE_REPLICA -> 1
            DataPlacementType.FULL_REPLICA -> deviceList.size
            DataPlacementType.CA_REPLICA -> dataPlacement.getContextAwareNumberOfCopies()
            DataPlacementType.CUSTOM_CA_REPLICA -> dataPlacement.getNumberOfCopies()
        }

    }

    private fun getUP(data: Data, deviceList: List<FogDevice>): MutableMap<String, Double> {
        val dataPlacement = DataPlacement(deviceList, data)
        val up: MutableMap<String, Double> = HashMap()
        for (device in deviceList) {
            up[device.id] = dataPlacement.getDataStorageLocation(
                device
            )
        }
        return up
    }


}
