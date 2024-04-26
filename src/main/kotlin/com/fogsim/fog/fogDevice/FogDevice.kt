package com.fogsim.fog.fogDevice

import com.fogsim.fog.core.models.*
import kotlin.random.Random

class FogDevice(
    var id: String,
    var name: String,
    val config: FogDeviceConfig,
    val storage: Double,
    var serverId: String,
    val type: FogDeviceType,
    val createdAt: Long = System.currentTimeMillis(),
    val location: MutableList<Double> = mutableListOf(0.0, 0.0),
    val availability:Double = Random.nextDouble(0.85,1.0),
    val accessibility:Double = Random.nextDouble(0.85,1.0),
    val deviceType:String = "",
) : BaseEntity, Device() {

    var status: EntityStatus = EntityStatus.SHUTDOWN
    var dataList = mutableListOf<Data>()
    @Transient
    var controller: FogDeviceController = FogDeviceController(this)
    override fun start() {
        controller.start()
    }

    override fun stop() {
        controller.stop()
    }

    override fun reset() {
        controller.stop()
        controller.start()
    }

    fun toJson(): String {
        return """
            {
                "id": "$id",
                "name": "$name",
                "config": ${config.toJson()},
                "storage": $storage,
                "serverId": "$serverId",
                "type": "${type.name}",
                "createdAt": $createdAt,
                "location": ${location},
                "availability": $availability,
                "accessibility": $accessibility
            }
        """.trimIndent()
    }
}
