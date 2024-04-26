package ir.fog.app.server

import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.app.device.CustomFogDevice
import ir.fog.entities.CustomTuple

class RuntimeAdaption(var serverId:String) {

    private var federatedLearning: FederatedLearning = FederatedLearning(serverId)
    private var tupleNumberOfCopiesMap: HashMap<String, MutableList<Int>> = hashMapOf()


    private fun getBestNumberOfCopies(
        tuple: CustomTuple, data: HashMap<String, MutableList<CustomFogDevice>>,
        availableDevices: MutableList<CustomFogDevice>, dataPlacementType: DataPlacementType
    ): Int {
        return federatedLearning.getBestNumberOfCopies(tuple, data, availableDevices, dataPlacementType)
    }

    private fun updateDeviceConfig(
        availableDevices: List<CustomFogDevice>, selectedDevice: List<CustomFogDevice>,
        taskPlacementType: TaskPlacementType
    ) {
        if (taskPlacementType == TaskPlacementType.CUSTOM_PERFORMANCE_AWARE) {
            federatedLearning.updateDeviceConfig(availableDevices, selectedDevice)
        }
    }

    fun handleNewDataForNumberOfCopies(
        tuple: CustomTuple, data: HashMap<String, MutableList<CustomFogDevice>>,
        availableDevices: MutableList<CustomFogDevice>, dataPlacementType: DataPlacementType
    ) {
        if (tupleNumberOfCopiesMap[tuple.ID].isNullOrEmpty()) {
            tupleNumberOfCopiesMap[tuple.ID] = mutableListOf()
            tupleNumberOfCopiesMap[tuple.ID]!!.add(data[tuple.ID]?.size ?: 0)
        }


        val n = getBestNumberOfCopies(tuple, data, availableDevices, dataPlacementType)

        if (n == 0) {
            print(n)
        }


        tupleNumberOfCopiesMap[tuple.ID]!!.add(n)



        if (tupleNumberOfCopiesMap.isNotEmpty() && tupleNumberOfCopiesMap[tuple.ID]!!.size > 1) {
            val size = tupleNumberOfCopiesMap[tuple.ID]!!.size
            if (tupleNumberOfCopiesMap[tuple.ID]!![size - 1] > tupleNumberOfCopiesMap[tuple.ID]!![size - 2]) {
                EventBus.sendEvent(
                    FogEvent(
                        tuple.ID,500.0, SimEvents.INCREASE_NUMBER_OF_TUPLE_COPIES,
                        tupleNumberOfCopiesMap[tuple.ID]!![size - 1] - tupleNumberOfCopiesMap[tuple.ID]!![size - 2],
                        currentEntityId = serverId,
                        nextEntity = serverId
                    )
                )
            } else if (tupleNumberOfCopiesMap[tuple.ID]!![size - 1] < tupleNumberOfCopiesMap[tuple.ID]!![size - 2]) {
                EventBus.sendEvent(
                    FogEvent(
                        tuple.ID,
                        0.0,
                        SimEvents.DECREASE_NUMBER_OF_TUPLE_COPIES,
                        tupleNumberOfCopiesMap[tuple.ID]!![size - 2] - tupleNumberOfCopiesMap[tuple.ID]!![size - 1],
                        currentEntityId = serverId,
                        nextEntity = serverId
                    )
                )
            } else {
                // do nothing
            }
        }


//        println("====================================================")
        println("============\uD83D\uDCD5:Number Of Copies===========")
        println(tupleNumberOfCopiesMap)
//        println("====================================================")

    }

    fun handleNewDataForUpdatingDeviceConfig(
        tuple: CustomTuple,
        data: HashMap<String, MutableList<CustomFogDevice>>,
        availableDevices: MutableList<CustomFogDevice>,
        taskPlacementType: TaskPlacementType
    ) {
        if (taskPlacementType == TaskPlacementType.CUSTOM_PERFORMANCE_AWARE) {
            val selectedDevices = data[tuple.ID]
            updateDeviceConfig(availableDevices, selectedDevices?.toList() ?: listOf(),taskPlacementType)
        }

    }

}
