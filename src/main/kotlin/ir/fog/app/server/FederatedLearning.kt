package ir.fog.app.server

import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.app.device.CustomFogDevice
import ir.fog.entities.CustomTuple
import ir.fog.placement.DataPlacement

/**
 * @author mohsen on 10/27/21
 */
class FederatedLearning(var serverId: String) {

    private var aTupleMap: HashMap<String, MutableList<Double>> = hashMapOf()
    private var bDeviceMap: HashMap<String, MutableList<Double>> = hashMapOf()
    private var nTupleMap: HashMap<String, Int> = hashMapOf()
    private var bTaskMap: HashMap<String, MutableList<Double>> = hashMapOf()

    fun getBestNumberOfCopies(
        tuple: CustomTuple,
        data: HashMap<String, MutableList<CustomFogDevice>>,
        availableDevices: MutableList<CustomFogDevice>, dataPlacementType: DataPlacementType
    ): Int {
        if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA) {
            checkConfigTupleNull(tuple, "a1")
            checkConfigTupleNull(tuple, "a2")
            checkConfigTupleNull(tuple, "a3")
            checkConfigTupleNull(tuple, "a4")
            checkConfigTupleNull(tuple, "a5")

            if (!data[tuple.ID].isNullOrEmpty()) {
                for (device in data[tuple.ID]!!) {
                    if (device.tupleList.isNotEmpty()) {
                        val list = device.getUpdatedTupleConfigs(tupleId = tuple.ID)
                        aTupleMap[tuple.ID + "a1"]!!.add(list[0])
                        aTupleMap[tuple.ID + "a2"]!!.add(list[1])
                        aTupleMap[tuple.ID + "a3"]!!.add(list[2])
                        aTupleMap[tuple.ID + "a4"]!!.add(list[3])
                        if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA)
                            aTupleMap[tuple.ID + "a5"]!!.add(list[4])
                    }

                }

                val config = tuple.configs

                config.a1 = findBestA(tuple, "a1")
                config.a2 = findBestA(tuple, "a2")
                config.a3 = findBestA(tuple, "a3")
                config.a4 = findBestA(tuple, "a4")
                config.a5 = findBestA(tuple, "a5")


                tuple.configs = config
            }

            EventBus.sendEvent(FogEvent(tuple.ID, 0.0, SimEvents.UPDATE_TUPLE_CONFIG, tuple.configs,currentEntityId = serverId,nextEntity = serverId))
        }


        val placement = getDataPlacement(tuple, availableDevices)

        return if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA) {
            placement.getNumberOfCopies()
        } else {
            placement.getContextAwareNumberOfCopies()
        }


    }


    fun updateDeviceConfig(
        availableDevices: List<CustomFogDevice>,
        selectedDevices: List<CustomFogDevice>
    ) {


        checkDeviceConfigNull("b1")
        checkDeviceConfigNull("b2")
        checkDeviceConfigNull("b3")
        checkDeviceConfigNull("b4")
        checkDeviceConfigNull("b5")


        checkTaskConfigNull("tb1")
        checkTaskConfigNull("tb2")
        checkTaskConfigNull("tb3")

        for (device in availableDevices) {
            val list = device.getUpdatedDeviceConfigs()
            bDeviceMap["b1"]!!.add(list[0])
            bDeviceMap["b2"]!!.add(list[1])
            bDeviceMap["b3"]!!.add(list[2])
            bDeviceMap["b4"]!!.add(list[3])
            bDeviceMap["b5"]!!.add(list[4])
        }


        for (device in selectedDevices) {
            val list = device.getUpdatedTaskConfigs()
            bTaskMap["tb1"]!!.add(list[0])
            bTaskMap["tb2"]!!.add(list[1])
            bTaskMap["tb3"]!!.add(list[2])
        }


        val tb1 = findBestTaskB("tb1")
        val tb2 = findBestTaskB("tb2")
        val tb3 = findBestTaskB("tb3")


        val b1 = findBestB("b1")
        val b2 = findBestB("b2")
        val b3 = findBestB("b3")
        val b4 = findBestB("b4")
        val b5 = findBestB("b5")

        for (device in selectedDevices) {
            EventBus.sendEvent(
                FogEvent(
                    device.name,
                    0.0,
                    SimEvents.UPDATE_DEVICE_TASK_CONFIG,
                    listOf(tb1, tb2, tb3),
                    currentEntityId = serverId,
                    nextEntity = device.id
                )
            )
        }

        for (device in availableDevices) {
            EventBus.sendEvent(
                FogEvent(
                    device.name,
                    0.0,
                    SimEvents.UPDATE_DEVICE_CONFIG,
                    listOf(b1, b2, b3, b4, b5),
                    currentEntityId = serverId,
                    nextEntity = device.id
                )
            )

        }


    }

    private fun findBestA(tuple: CustomTuple, name: String): Double {
        val values = aTupleMap[tuple.ID + name]
        return (values!!.sum() / values.size)
    }

    private fun findBestB(key: String): Double {
        val values = bDeviceMap[key]
        return (values!!.sum() / values.size)
    }

    private fun findBestTaskB(key: String): Double {
        val values = bTaskMap[key]
        return (values!!.sum() / values.size)
    }

    private fun checkConfigTupleNull(tuple: CustomTuple, name: String) {
        if (aTupleMap[tuple.ID + name].isNullOrEmpty()) {
            aTupleMap[tuple.ID + name] = mutableListOf()
        }
    }

    private fun checkDeviceConfigNull(key: String) {
        if (bDeviceMap[key].isNullOrEmpty()) {
            bDeviceMap[key] = mutableListOf()
        }
    }

    private fun checkTaskConfigNull(key: String) {
        if (bTaskMap[key].isNullOrEmpty()) {
            bTaskMap[key] = mutableListOf()
        }
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
}


/**
 *
 * The weights allow programmers to adjust the replication strategy to the needs of their system.
 * For instance, in systems with large bandwidths that focus on fast execution of time- critical tasks,α1-a4 might be positive.
 * With increasing data size, the number of replicas also increases as migrating aborted tasks becomes particularly costly.
 * Systems with low bandwidth or limited storage capacities may use a negative α1-a4 to decrease the data transfer overhead.
 */

/**
 * α2
 *
 */
