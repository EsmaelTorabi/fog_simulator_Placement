package ir.fog.core

import java.util.*

data class FogEvent(
    var entityId: String,
    var delay: Double,
    var eventType: SimEvents,
    var data: Any? = null,
    var date: Date = Date(),
    var currentEntityId: String? = null,
    var nextEntity: String? = null,
    var clusterId:String = "" // todo add id here
)
