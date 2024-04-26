package com.fogsim.fog.server

import com.fogsim.fog.LogManager
import com.fogsim.fog.core.models.SensorType
import com.fogsim.fog.fogDevice.FogDevice

class ServerLogger(val server: Server) {
    private var deviceTaskCounterMap = mutableMapOf<String, Int>()
    private var sensorTypeRuntimeMap: HashMap<SensorType, MutableList<Long>> = hashMapOf()
    private var sensorTypeStorageCostMap: HashMap<SensorType, Double> = hashMapOf()

    private fun logDeviceTaskCounterMap() {
        println("DeviceTaskCounterMap:")
        deviceTaskCounterMap.forEach { (key, value) ->
            println("$key: $value")
        }
    }


    fun increaseDeviceTaskCounter(deviceId: String) {
        if (deviceTaskCounterMap.containsKey(deviceId)) {
            deviceTaskCounterMap[deviceId] = deviceTaskCounterMap[deviceId]!! + 1
        } else {
            deviceTaskCounterMap[deviceId] = 1
        }
    }

    fun addSensorTypeRuntime(sensorType: SensorType, runtime: Long) {
        if (sensorTypeRuntimeMap.containsKey(sensorType)) {
            sensorTypeRuntimeMap[sensorType]!!.add(runtime)
        } else {
            sensorTypeRuntimeMap[sensorType] = mutableListOf(runtime)
        }
    }

    private fun getAverageRuntime(sensorType: SensorType): Long {
        if (sensorTypeRuntimeMap.containsKey(sensorType)) {
            val runtimeList = sensorTypeRuntimeMap[sensorType]!!
            return runtimeList.sum() / runtimeList.size
        }
        return 0
    }

    private fun getTotalAverageRunTime(): Long {
        var total = 0L
        for (sensorType in SensorType.values()) {
            total += getAverageRuntime(sensorType)
        }
        return total
    }

    fun addSensorTypeStorageCost(sensorType: SensorType, storageCost: Double) {
        if (sensorTypeStorageCostMap.containsKey(sensorType)) {
            sensorTypeStorageCostMap[sensorType] = sensorTypeStorageCostMap[sensorType]!! + storageCost
        } else {
            sensorTypeStorageCostMap[sensorType] = storageCost
        }
    }

    private fun getAverageStorageCost(sensorType: SensorType): Double {
        if (sensorTypeStorageCostMap.containsKey(sensorType)) {
            val storageCost = sensorTypeStorageCostMap[sensorType]!!
            return storageCost / deviceTaskCounterMap.size
        }
        return 0.0
    }


    private fun getTotalAverageStorageCost(): Double {
        var total = 0.0
        for (sensorType in SensorType.values()) {
            total += getAverageStorageCost(sensorType)
        }
        return total
    }

    fun logAllResults(deviceList: List<FogDevice>) {
        var totalUploadCost = 0.0
        var totalDownloadCost = 0.0
        var totalEnergyCost = 0.0
        var totalTransferCost = 0.0
        // list of map for each device
        var deviceTaskCounterMapList = mutableListOf<Pair<String, Double>>()
        var cpuCost = 0.0
        var networkCost = 0.0

        for (device in deviceList) {
            deviceTaskCounterMapList.add(
                device.name to if (device.controller!!.resourceLogger.totalData.toDouble() == 0.0) 1.0 else device.controller!!.resourceLogger.totalAcceptedData.toDouble() / if (device.controller!!.resourceLogger.totalData.toDouble() == 0.0) 1.0 else device.controller!!.resourceLogger.totalData.toDouble())
            totalEnergyCost = device.controller!!.resourceLogger.getEnergyUsage()
            totalUploadCost += device.controller!!.resourceLogger.uploadCost
            totalDownloadCost += device.controller!!.resourceLogger.downloadCost
            totalTransferCost += device.controller!!.resourceLogger.transferCost
            networkCost += device.controller!!.resourceLogger.networkCost

            cpuCost += device.controller!!.resourceLogger.totalCpuCost
        }
        logDeviceTaskCounterMap()

        LogManager.logResults(
            uploadCost = totalUploadCost,
            downloadCost = totalDownloadCost,
            energyCost = totalEnergyCost,
            transferCost = totalTransferCost,
            runtime = getTotalAverageRunTime(),
            storageCost = getTotalAverageStorageCost(),
            accessibility = deviceTaskCounterMapList,
            availability = deviceTaskCounterMapList,
            totalCpuTime = cpuCost/deviceList.size,
            networkCost = networkCost/deviceList.size
        )
    }


}
