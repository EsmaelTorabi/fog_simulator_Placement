package ir.fog.placement

import ir.fog.app.device.CustomFogDevice
import ir.fog.entities.CustomFogDeviceCharacteristics
import ir.fog.entities.CustomTuple


class TaskPlacement(
    var device: CustomFogDevice? = null,
    var tuple: CustomTuple? = null,
    var b1: Double = 0.0,
    var b2: Double = 0.0,
    var b3: Double = 0.0
) {
    fun getDeviceAvailability(): Double {
        val characteristics: CustomFogDeviceCharacteristics =
            device!!.characteristics
        return b1 * device!!.host.totalMips - b2 * device!!.queueCount + b3 * characteristics.RPI
    }
}



//d1,d2,d3,d4,d5,d6,d7,d8,d9



//d1,d2,d3,d4,d5,d6


// 10,90,4,12,23,13





//d3,d1,d4,d6,d5,d2
