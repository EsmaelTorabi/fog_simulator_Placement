package com.fogsim.fog.server.placement

import com.fogsim.fog.Application
import com.fogsim.fog.EventEmitter
import com.fogsim.fog.core.models.*
import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.server.ServerDataHandler
import com.fogsim.fog.server.ServerDeviceHandler
import com.fogsim.fog.server.ServerEvent
import java.util.*

class RuntimeAdaption(
    val serverId: String,
    val dataHandler: ServerDataHandler,
    val deviceHandler: ServerDeviceHandler,
    val deviceList: List<FogDevice>
) {
    var isStarted = false;
    private var federatedLearning: FederatedLearning = FederatedLearning(serverId)
    private var dataNumberOfCopiesMap: EnumMap<SensorType, MutableList<Int>> = EnumMap(SensorType::class.java)
    private val timer = Timer()
    fun start() {
        val task = object : TimerTask() {
            override fun run() {
                for (data in dataHandler.dataSet) {
                    handleNewDataForUpdatingDeviceConfig(
                        data,
                        deviceHandler.dataDeviceListMap,
                        deviceList.filter { it.type != FogDeviceType.Warner }.toMutableList(),
                        Application.taskPlacementType
                    )
                    handleNewDataForNumberOfCopies(
                        data,
                        deviceHandler.dataDeviceListMap,
                        deviceList.filter { it.type != FogDeviceType.Warner }.toMutableList(),
                        Application.dataPlacementType
                    )
                }
            }
        }
        timer.schedule(task, 0, 5000)

    }

    fun stop() {
        timer.cancel()
    }


    private fun getBestNumberOfCopies(
        data: Data, dataDeviceListMap: HashMap<String, MutableList<FogDevice>>,
        availableDevices: MutableList<FogDevice>, dataPlacementType: DataPlacementType
    ): Int {
        return federatedLearning.getBestNumberOfCopies(data, dataDeviceListMap, availableDevices, dataPlacementType)
    }

    fun handleNewDataForNumberOfCopies(
        data: Data, dataDeviceListMap: HashMap<String, MutableList<FogDevice>>,
        availableDevices: MutableList<FogDevice>, dataPlacementType: DataPlacementType
    ) {
        if (dataNumberOfCopiesMap[data.sensorType].isNullOrEmpty()) {
            dataNumberOfCopiesMap[data.sensorType] = mutableListOf()
            dataNumberOfCopiesMap[data.sensorType]!!.add(dataDeviceListMap[data.id]?.size ?: 0)
        }


        val n = getBestNumberOfCopies(data, dataDeviceListMap, availableDevices, dataPlacementType)


        dataNumberOfCopiesMap[data.sensorType]!!.add(n)

        if (dataNumberOfCopiesMap.isNotEmpty() && dataNumberOfCopiesMap[data.sensorType]!!.size > 1) {
            val size = dataNumberOfCopiesMap[data.sensorType]!!.size
            if (dataNumberOfCopiesMap[data.sensorType]!![size - 1] > dataNumberOfCopiesMap[data.sensorType]!![size - 2]) {
                val event =
                    ServerEvent.IncreaseNumberOfCopies(
                        targetData = data,
                        dataNumberOfCopiesMap[data.sensorType]!![size - 1] - dataNumberOfCopiesMap[data.sensorType]!![size - 2]
                    )
                event.receiverId = serverId
                event.senderId = serverId
                event.eventType = EventType.RuntimeAdaption
                EventEmitter.emit(event)

            } else if (dataNumberOfCopiesMap[data.sensorType]!![size - 1] < dataNumberOfCopiesMap[data.sensorType]!![size - 2]) {
                val event =
                    ServerEvent.DecreaseNumberOfCopies(
                        data,
                        dataNumberOfCopiesMap[data.sensorType]!![size - 2] - dataNumberOfCopiesMap[data.sensorType]!![size - 1]
                    )
                event.receiverId = serverId
                event.senderId = serverId
                event.eventType = EventType.RuntimeAdaption
                EventEmitter.emit(event)

            } else {
                // do nothing
            }
        }


//        println("====================================================")
        println("============\uD83D\uDCD5:Number Of Copies===========")
        println(dataNumberOfCopiesMap)
//        println("====================================================")

    }

    fun handleNewDataForUpdatingDeviceConfig(
        data: Data,
        dataDeviceListMap: HashMap<String, MutableList<FogDevice>>,
        availableDevices: MutableList<FogDevice>,
        taskPlacementType: TaskPlacementType
    ) {
        if (taskPlacementType == TaskPlacementType.CUSTOM_PERFORMANCE_AWARE) {
            val selectedDevices = dataDeviceListMap[data.id]
            updateDeviceConfig(availableDevices, selectedDevices?.toList() ?: listOf(), taskPlacementType)
        }

    }

    private fun updateDeviceConfig(
        availableDevices: List<FogDevice>, selectedDevice: List<FogDevice>,
        taskPlacementType: TaskPlacementType
    ) {
        if (taskPlacementType == TaskPlacementType.CUSTOM_PERFORMANCE_AWARE) {
            federatedLearning.updateDeviceConfig(availableDevices, selectedDevice)
        }
    }

}
