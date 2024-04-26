package com.fogsim.fog.fogDevice

import com.fogsim.fog.core.Constants.DEFAULT_TRANSFER_COST

class DeviceResourceLogger(val device: FogDevice) {
    var cpuUsage: Double = 0.0
    var ramUsage: Double = 0.0
    var emptySpace: Double = 0.0
    var uploadCost: Double = 0.0
    var downloadCost: Double = 0.0
    var networkCost: Double = 0.0
    var queueCount: Int = 0
    var totalBusyTime: Double = 0.0
    var totalIdleTime: Double = 0.0
    var transferCost = 0.0
    var totalData = 0
    var totalAcceptedData = 0
    var totalRejectedData = 0
    var totalCpuCost = 0.0


    init {
        emptySpace = device.storage
    }

    fun addCpuUsage(usage: Double) {
        cpuUsage += usage
    }

    fun addRamUsage(usage: Double) {
        ramUsage += usage
    }

    fun reduceEmptySpace(space: Double) {
        emptySpace -= space
    }

    fun increaseEmptySpace(space: Double) {
        emptySpace += space
    }

    fun addUploadCost(dataSize: Double) {
        addNetworkCost(dataSize)
        val cost = dataSize / device.config.uploadBW
        uploadCost += cost
    }

    fun addDownloadCost(dataSize: Double) {
        addNetworkCost(dataSize)
        val cost = dataSize / device.config.downloadBW
        downloadCost += cost
    }

    fun addTransferCost(distance: Double, dataSize: Double) {
        val cost = distance * (dataSize/8) * DEFAULT_TRANSFER_COST
        transferCost += cost
    }

    fun addNetworkCost(cost: Double) {
        networkCost += cost
    }

    fun addBusyTime(time: Double) {
        totalBusyTime += time
    }

    fun getEnergyUsage(): Double {
        val runDuration = System.currentTimeMillis() - device.createdAt
        val idleDuration = runDuration - totalBusyTime
        return (totalBusyTime * device.config.costPerBusyTime + idleDuration * device.config.costPerIdleTime) / 1000
    }

    fun increaseDataCount() {
        totalData += 1
    }

    fun increaseAcceptedDataCount() {
        totalAcceptedData += 1
    }

    fun increaseRejectedDataCount() {
        totalRejectedData += 1
    }

    fun increaseCpuCost(value:Double){
        totalCpuCost += value
    }

}