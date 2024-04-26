package ir.fog.entities

import org.cloudbus.cloudsim.Host
import org.fog.entities.FogDeviceCharacteristics
import java.io.Serializable

class CustomFogDeviceCharacteristics(
    architecture: String?, os: String?, vmm: String?, host: Host?,
    timeZone: Double, costPerMips: Double, costPerMem: Double,
    costPerStorage: Double, costPerBw: Double,
    var RPI: Int = 2, var flu: Double, var b1: Double, var b2: Double, var b3: Double
) :Serializable
