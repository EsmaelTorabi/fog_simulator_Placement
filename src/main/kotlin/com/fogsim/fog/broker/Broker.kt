package com.fogsim.fog.broker

import com.fogsim.fog.core.models.BaseEntity
import com.fogsim.fog.core.models.EntityStatus

class Broker(
    var id: String,
    var name: String,
    var serverId:String,
    val delay: Int,
    val validSize: List<Long>,
    val validCpuLength: List<Long>,
) : BaseEntity {
    var status: EntityStatus = EntityStatus.SHUTDOWN
    @Transient
    var controller: BrokerController? = null

    override fun start() {
        controller = BrokerController(this)
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
                "serverId": "$serverId",
                "delay": $delay
            }
        """.trimIndent()
    }
}
