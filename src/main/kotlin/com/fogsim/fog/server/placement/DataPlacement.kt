package com.fogsim.fog.server.placement

import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.utils.DataUtils
import com.fogsim.fog.fogDevice.FogDevice
import kotlin.math.roundToInt


class DataPlacement(
    var deviceList: List<FogDevice?>,
    var data: Data,
) {
    private var SD: Double = DataUtils.dataSizeInBytes(data.value).toDouble()
    private var SMax: Double = data.config.SMax
    private var a1: Double = data.config.a1
    private var a2: Double = data.config.a2
    private var a3: Double = data.config.a3
    private var a4: Double = data.config.a4
    private var a5: Double = data.config.a5
    private var FAva: Double = data.config.FAva
    private var FPar: Double = data.config.FPar
    private var normalizedCount: Double = data.config.normalizedCount


    fun getContextAwareNumberOfCopies(): Int {
        return ((a1 * getDataCapacity() + a2 * getCapCapacity() + a3 * getFluCapacity() + a4 * getAppCapacity()) * getDeviceCountAndNormalizedDevices()).toInt()
    }

    fun getNumberOfCopies(): Int {
        return ((a1 * getDataCapacity() + a2 * getCapCapacity() + a3 * getFluCapacity() + a4 * getAppCapacity() +
                a5 * getEdCapacity()) * getDeviceCountAndNormalizedDevices()).roundToInt()
    }

    fun getDataStorageLocation(
        device: FogDevice
    ): Double {
        return (device.config.db1 * (device.config.devicePresenceMiddle - device.config.currentAttendanceTime) + device.config.db2 * device.config.devicesPresenceVariance
                + device.config.db3 * device.controller!!.resourceLogger.emptySpace) - device.config.db4 * device.controller!!.resourceLogger.queueCount + device.config.db5 * device.config.RPI
    }

    /**
     * Relative size of data
     */
    private fun getDataCapacity(): Double {
        return if (SMax > 0) {
            SD / SMax
        } else 0.0
    }

    /**
     * Relative size of storage
     */
    private fun getCapCapacity(): Double {
        var cf = 0.0
        var ct = 0.0
        for (device in deviceList) {
            cf += device!!.storage
            ct += device.controller!!.resourceLogger.emptySpace
        }
        return if (ct > 0) {
            cf / ct
        } else 0.0
    }

    /**
     * Oscillation coefficient: the median time of presence of each device in a time window
     */
    private fun getFluCapacity(): Double {

        var flu = 0.0
        for (device in deviceList) {
            flu += device!!.config.flu
        }
        return flu / deviceList.size
    }

    /**
     * Program coefficient
     */
    private fun getAppCapacity(): Double {
        return (FAva + FPar) / 2
    }

    private fun getEdCapacity(): Double {
        return data.expireTime
    }

    private fun getDeviceCountAndNormalizedDevices(): Double {
        normalizedCount = 0.9 * deviceList.size
        return deviceList.size / normalizedCount
    }

    private fun getDevicesStorage(): Double {
        var storage = 0.0
        for (device in deviceList) {
            storage += device!!.controller!!.resourceLogger.emptySpace
        }
        return storage
    }

    private fun getDevicesQueueCount(): Int {
        var queueCount = 0
        for (device in deviceList) {
            queueCount += device!!.controller!!.resourceLogger.queueCount
        }
        return queueCount
    }

    private fun getDevicesRPI(): Int {
        var averageRPI = 0
        for (device in deviceList) {
            averageRPI += device!!.config.RPI
        }
        return averageRPI / deviceList.size
    }

    override fun toString(): String {
        return "DataPlacement(a1=$a1, a2=$a2, a3=$a3, a4=$a4, a5=$a5)"
    }
}
