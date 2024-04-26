package com.fogsim.fog.core.models

data class Data(
    val id: String,// 10
    var value: String, /// 010101010101
    val sensorType: SensorType, // Heat
    var config: DataConfig = DataConfig(),
    var expireTime: Double = 0.02,
    var analyzeState: TaskAnalyzeState = TaskAnalyzeState.NOT_ANALYZED,
    var processTime:Long = 3000L
){
    fun copy(): Data {
        return Data(
            id = id,
            value = value,
            sensorType = sensorType,
            config = config.copy(),
            expireTime = expireTime,
            analyzeState = analyzeState,
            processTime = processTime
        )
    }

    fun toJson(): String {
        return """
            {
                "id": "$id",
                "value": "$value",
                "sensorType": "${sensorType.name}",
                "config": ${config.toJson()},
                "expireTime": $expireTime,
                "analyzeState": "${analyzeState.name}",
                "processTime": "${processTime}"
            }
        """.trimIndent()
    }
}
