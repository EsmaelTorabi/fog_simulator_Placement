package com.fogsim.fog.sensor

import com.fogsim.fog.core.models.BaseEntity
import com.fogsim.fog.core.models.DataConfig
import com.fogsim.fog.core.models.SensorType

class Sensor(
    var id: String, // C1-sensor-1
    var name: String, // heat1
    var gatewayId: String, // C1-gateway
    val characteristics: List<SensorCharacteristic>,
    val latency: Int,
    val frequency: Int,
    val availability: Double,
    val sensorType: SensorType,// Heat
    val dataConfig: DataConfig,
    val dataProcessTime:Long = 3000L

    ) : BaseEntity {
    @Transient
    var controller :SensorController? = null


    override fun start() {
        controller = SensorController(this)
        controller?.start()
    }

    override fun stop() {
        controller?.stop()
    }

    override fun reset() {
        controller?.stop()
        controller?.start()
    }
    fun toJson(): String {
        return """
            {
                "id": "$id",
                "name": "$name",
                "gatewayId": "$gatewayId",
                "latency": $latency,
                "frequency":$frequency,
                "availability":$availability,
                "sensorType":${sensorType.name},
                "dataConfig":${dataConfig.toJson()},
                "dataProcessTime":$dataProcessTime,
                "characteristics": ${characteristics.map { it.toJson() }}
            }
        """.trimIndent()
    }

}
