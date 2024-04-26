package com.fogsim.fog.cluster

import com.fogsim.fog.broker.Broker
import com.fogsim.fog.core.models.EntityStatus
import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.gateway.Gateway
import com.fogsim.fog.sensor.Sensor
import com.fogsim.fog.server.Server

class Cluster(
    val id: String,
    val name: String,
    val server: Server,
    val broker: Broker,
    val gateway: Gateway,
    val fogDeviceList: MutableList<FogDevice>,
    val sensorList: MutableList<Sensor>
) {
    var status: EntityStatus = EntityStatus.SHUTDOWN
    @Transient
    var controller: ClusterController? = null

    fun start() {
        controller = ClusterController(this)
        controller?.start()
    }

    fun stop() {
        controller?.stop()
    }

    fun reset() {
        controller?.reset()
    }



    fun toJson(): String {
        return """{
                "id": "$id",
                "name": "$name",
                "server": ${server.toJson()},
                "broker": ${broker.toJson()},
                "gateway": ${gateway.toJson()},
                "fogDeviceList": ${fogDeviceList.map { it.toJson() }},
                "sensorList": ${sensorList.map { it.toJson() }}
            }""".trimIndent()
    }

    override fun toString(): String {
        return "Cluster(id='$id', name='$name', server=$server, broker=$broker, gateway=$gateway, fogDeviceList=$fogDeviceList, sensorList=$sensorList, status=$status, controller=$controller)"
    }
}
