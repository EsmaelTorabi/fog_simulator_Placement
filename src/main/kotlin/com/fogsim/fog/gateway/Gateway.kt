package com.fogsim.fog.gateway

import com.fogsim.fog.core.models.BaseEntity

class Gateway(
    var id: String,
    var name: String,
    var brokerId: String,
    val delay: Int,

    ) : BaseEntity {
    @Transient
    var controller : GatewayController? = null
    override fun start() {
        controller = GatewayController(this)
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
                "brokerId": "$brokerId",
                "delay": $delay
            }
        """.trimIndent()
    }
}
