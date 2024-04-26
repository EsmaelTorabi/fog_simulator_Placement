package ir.fog.entities

import ir.fog.app.broker.CustomBroker
import ir.fog.app.device.CustomFogDevice
import ir.fog.app.gateway.Gateway
import ir.fog.app.sensor.CustomSensor
import ir.fog.app.server.Server
import java.io.Serializable


class Cluster(
    var id: String,
    var name: String,
    var fogDeviceList: List<CustomFogDevice>?=null,
    var server: Server?= null,
    var neighbors: List<Cluster>? = null,
    var totalSpace: Int = 0,
    var emptySpace: Int = 0,
    var broker: CustomBroker? = null,
    var sensorList: List<CustomSensor>?=null,
    var gateway: Gateway?=null,
    var maxSimulationTime: Int = 60

) : Serializable
