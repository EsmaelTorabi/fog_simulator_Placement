package com.fogsim.fog.sensor

class SensorCharacteristic(
    val cpu: Int,
    val ram: Int,
    val bandwidth: Int,
    val power: Int,
    ){
    fun toJson(): String {
        return """
            {
                "cpu": $cpu,
                "ram": $ram,
                "bandwidth": $bandwidth,
                "power": $power
            }
        """.trimIndent()
    }
}
