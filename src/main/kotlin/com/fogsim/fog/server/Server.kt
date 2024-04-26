package com.fogsim.fog.server

import com.fogsim.fog.core.models.BaseEntity
import com.fogsim.fog.core.models.Device
import com.fogsim.fog.core.models.EntityStatus
import com.fogsim.fog.fogDevice.FogDevice

class Server(
    var id: String,
    var name: String,
    val delay: Int,
    var deviceList: MutableList<FogDevice>,
    val location:MutableList<Double> = mutableListOf(0.0,0.0)
) : BaseEntity, Device() {
    var status: EntityStatus = EntityStatus.SHUTDOWN
    @Transient
    var controller: ServerController? = null
    override fun start() {
        controller = ServerController(this)
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
                "delay": $delay,
                "deviceList": ${deviceList.map { it.toJson() }},
                "location": ${location}
            }
        """.trimIndent()
    }
}
