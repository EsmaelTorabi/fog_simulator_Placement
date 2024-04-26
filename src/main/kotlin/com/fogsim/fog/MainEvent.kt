package com.fogsim.fog

import com.fogsim.AppResult
import com.fogsim.fog.cluster.Cluster
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.EventType
import com.fogsim.fog.core.models.SensorType

open class MainEvent {
    var id: String = "0"
    var data: Data = Data(id="", value = "", sensorType = SensorType.TEMPERATURE)
    var senderId: String = "0"
    var receiverId: String = "0"
    var delay: Int = 0
    var date: Long = System.currentTimeMillis()
    var eventType: EventType = EventType.NONE
    var clusterList:List<Cluster>? = null
    var result: AppResult? = null

    override fun toString(): String {
        return "MainEvent(id='$id', data=$data, senderId='$senderId', receiverId='$receiverId', delay=$delay, date=$date)"
    }
    fun toJson(): String {
        return """
            {
                "id": "$id",
                "data": ${data.toJson()},
                "senderId": "$senderId",
                "receiverId": "$receiverId",
                "delay": $delay,
                "date": $date,
                "eventType": "${eventType.name}",
                "clusterList": ${clusterList?.map { it.toJson() }},
                "result": ${result?.toJson()}
            }
        """.trimIndent()
    }
}
