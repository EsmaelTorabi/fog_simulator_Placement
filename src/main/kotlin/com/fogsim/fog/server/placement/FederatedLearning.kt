package com.fogsim.fog.server.placement

import com.fogsim.fog.EventEmitter
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.DataPlacementType
import com.fogsim.fog.core.models.EventType
import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.fogDevice.FogDeviceEvent
import com.fogsim.fog.server.ServerEvent

class FederatedLearning(var serverId: String) {

    private var aDataMap: HashMap<String, MutableList<Double>> = hashMapOf()
    private var bDeviceMap: HashMap<String, MutableList<Double>> = hashMapOf()
    private var bTaskMap: HashMap<String, MutableList<Double>> = hashMapOf()
    private var configHandler: ConfigHandler = ConfigHandler()
    fun getBestNumberOfCopies(
        data: Data,
        dataDeviceListMap: HashMap<String, MutableList<FogDevice>>,
        availableDevices: MutableList<FogDevice>, dataPlacementType: DataPlacementType
    ): Int {
        if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA) {
            initialDataConfog(data, "a1")
            initialDataConfog(data, "a2")
            initialDataConfog(data, "a3")
            initialDataConfog(data, "a4")
            initialDataConfog(data, "a5")

            if (!dataDeviceListMap[data.id].isNullOrEmpty()) {
                for (device in dataDeviceListMap[data.id]!!) {
                    if (device.controller!!.dataHandler.getRawDataList().isNotEmpty()) {
                        val list = configHandler.getUpdatedTupleConfigs(data.id, device)
                        aDataMap[data.sensorType.name + "a1"]!!.add(list[0])
                        aDataMap[data.sensorType.name + "a2"]!!.add(list[1])
                        aDataMap[data.sensorType.name + "a3"]!!.add(list[2])
                        aDataMap[data.sensorType.name + "a4"]!!.add(list[3])
                        aDataMap[data.sensorType.name + "a5"]!!.add(list[4])
                    }

                }

                val config = data.config.copy()

                config.a1 = findBestA(data, "a1")
                config.a2 = findBestA(data, "a2")
                config.a3 = findBestA(data, "a3")
                config.a4 = findBestA(data, "a4")
                config.a5 = findBestA(data, "a5")


                data.config = config
            }

            val deviceEvent = FogDeviceEvent.UpdateDataConfig(data.id, data.config)
            deviceEvent.senderId = "FederatedLearning"
            deviceEvent.eventType = EventType.RuntimeAdaption
            for (device in dataDeviceListMap[data.id] ?: emptyList()) {
                deviceEvent.receiverId = device.id
                EventEmitter.emit(deviceEvent)
            }
            val sererEvent = ServerEvent.UpdateDataConfig(data.id, data.config)
            sererEvent.senderId = "FederatedLearning"
            sererEvent.eventType = EventType.RuntimeAdaption
            sererEvent.receiverId = serverId
            EventEmitter.emit(sererEvent)

        }


        val placement = DataPlacement(availableDevices, data)

        return if (dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA) {
            placement.getNumberOfCopies()
        } else {
            placement.getContextAwareNumberOfCopies()
        }


    }


    fun updateDeviceConfig(
        availableDevices: List<FogDevice>,
        selectedDevices: List<FogDevice>
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
            val list = configHandler.getUpdatedDeviceConfigs(device)
            bDeviceMap["b1"]!!.add(list[0])
            bDeviceMap["b2"]!!.add(list[1])
            bDeviceMap["b3"]!!.add(list[2])
            bDeviceMap["b4"]!!.add(list[3])
            bDeviceMap["b5"]!!.add(list[4])
        }


        for (device in selectedDevices) {
            val list = configHandler.getUpdatedTaskConfigs(device)
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
            val event = FogDeviceEvent.UpdateDeviceConfig(listOf(b1, b2, b3, b4, b5), listOf(tb1, tb2, tb3))
            event.eventType = EventType.RuntimeAdaption
            event.senderId = serverId
            event.receiverId = device.id

            EventEmitter.emit(event)
        }


    }

    private fun findBestA(data: Data, name: String): Double {
        val values = aDataMap[data.sensorType.name + name]
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

    private fun initialDataConfog(data: Data, name: String) {
        if (aDataMap[data.sensorType.name + name].isNullOrEmpty()) {
            aDataMap[data.sensorType.name + name] = mutableListOf()
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


}


