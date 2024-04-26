package com.fogsim.fog.server.placement

import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.fogDevice.FogDeviceConfig


class TaskPlacement(
    var device: FogDevice? = null
) {
    fun getDeviceAvailability(): Double {
        val config: FogDeviceConfig =
            device!!.config
        return config.tb1 * config.totalMips - config.tb2 * device!!.controller!!.resourceLogger.queueCount + config.tb3 * config.RPI
    }
}
