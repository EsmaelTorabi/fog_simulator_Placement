package com.fogsim.fog.core.models

data class AnalyzedData(
    val id: String,
    val sensorType: SensorType,
    val value: Double,
    val data: Data,
    val runtime: Long = 0,
    val size:Double = 0.0
) {
    fun toJson(): String {
        return """
            {
                "id": "$id",
                "sensorType": "${sensorType.name}",
                "value": $value,
                "size":$size,
                "data": ${data.toJson()},
                "runtime": $runtime
            }
        """.trimIndent()
    }
}
