package ir.fog.placement

import ir.fog.app.device.CustomFogDevice
import ir.fog.entities.CustomTuple


class DataPlacement(
    var deviceList: List<CustomFogDevice?>,
    var tuple: CustomTuple,
    var SD: Double,
    var SMax: Double,
    var a1: Double,
    var a2: Double,
    var a3: Double,
    var a4: Double,
    var a5: Double,
    var FAva: Double,
    var FPar: Double,
    var normalizedCount: Double
) {


    fun getContextAwareNumberOfCopies(): Int {
        return ((a1 * getDataCapacity() + a2 * getCapCapacity() + a3 * getFluCapacity() + a4 * getAppCapacity()) * getDeviceCountAndNormalizedDevices()).toInt()
    }

    fun getNumberOfCopies(): Int {
        return ((a1 * getDataCapacity() + a2 * getCapCapacity() + a3 * getFluCapacity() + a4 * getAppCapacity() +
                a5 * getEdCapacity()) * getDeviceCountAndNormalizedDevices()).toInt()
    }

    fun getDataStorageLocation(
        device: CustomFogDevice, b1: Double, b2: Double,
        b3: Double, b4: Double, b5: Double, currentAttendanceTime: Double, devicePresenceMiddle: Double,
        devicesPresenceVariance: Double
    ): Double {
        return (b1 * (devicePresenceMiddle - currentAttendanceTime) + b2 * devicesPresenceVariance
                + b3 * device.host.storage) - b4 * device.queueCount + b5 * device.characteristics.RPI
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
            cf += device!!.host.storage.toDouble()
            ct += device.emptySpace
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
            flu += device!!.characteristics.flu
        }
        return flu/deviceList.size
    }

    /**
     * Program coefficient
     */
    private fun getAppCapacity(): Double {
        return (FAva + FPar) / 2
    }

    private fun getEdCapacity(): Double {
        return tuple.expireTime
    }

    private fun getDeviceCountAndNormalizedDevices(): Double {
        normalizedCount=0.9 * deviceList.size
        return deviceList.size / normalizedCount
    }

    private fun getDevicesStorage(): Double {
        var storage = 0.0
        for (device in deviceList) {
            storage += device!!.host.storage.toDouble()
        }
        return storage
    }

    private fun getDevicesQueueCount(): Int {
        var queueCount = 0
        for (device in deviceList) {
            queueCount += device!!.queueCount
        }
        return queueCount
    }

    private fun getDevicesRPI(): Int {
        var averageRPI = 0
        for (device in deviceList) {
            averageRPI += device!!.characteristics.RPI
        }
        return averageRPI / deviceList.size
    }

    override fun toString(): String {
        return "DataPlacement(a1=$a1, a2=$a2, a3=$a3, a4=$a4, a5=$a5)"
    }
}
