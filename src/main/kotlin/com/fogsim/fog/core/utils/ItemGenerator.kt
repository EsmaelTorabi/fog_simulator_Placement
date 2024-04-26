package com.fogsim.fog.core.utils

import com.fogsim.fog.broker.Broker
import com.fogsim.fog.cluster.Cluster
import com.fogsim.fog.core.Generator
import com.fogsim.fog.core.models.DataConfig
import com.fogsim.fog.core.models.FogDeviceType
import com.fogsim.fog.core.models.SensorType
import com.fogsim.fog.core.roundTo
import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.fogDevice.FogDeviceConfig
import com.fogsim.fog.fogDevice.FogDeviceController
import com.fogsim.fog.gateway.Gateway
import com.fogsim.fog.sensor.Sensor
import com.fogsim.fog.sensor.SensorCharacteristic
import com.fogsim.fog.server.Server
import kotlin.random.Random

class ItemGenerator(private val runDuration:Long) {

     fun generateClusterList(count: Int): MutableList<Cluster> {
        val list = mutableListOf<Cluster>()
        for (i in 1..count) {
            val gateway = Gateway(
                Generator.gatewayID(i),
                "Gateway C${i}",
                "C${i}B",
                1000
            )
            val broker = Broker(
                Generator.brokerID(i),
                "Broker C${i}",
                "C${i}S",
                1000,
                mutableListOf(10L , 8000L),
                mutableListOf(100L , 2000L)
            )
            val deviceList = generateDeviceList("C$i", 10)
            val sensorList =
                generateSensorList("C$i", 10, listOf(SensorType.SMOKE, SensorType.TEMPERATURE, SensorType.GAS))

            val server = Server(
                id = Generator.serverID(i),
                name = "Server C${i}",
                delay = 1000,
                deviceList = deviceList,
                location = mutableListOf(Random.nextDouble(0.0, 100.0), Random.nextDouble(0.0, 100.0))
            )

            val cluster = Cluster(
                id = "C$i",
                name = "Cluster$i",
                server = server,
                broker = broker,
                gateway = gateway,
                fogDeviceList = deviceList,
                sensorList = sensorList
            )
            list.add(cluster)
        }
        return list
    }
    fun addClusterList(clusterList:MutableList<Cluster>): MutableList<Cluster>{
        val list = mutableListOf<Cluster>()
        clusterList.forEachIndexed {index, item ->
            val gateway = clusterList[index].gateway
            gateway.id =  Generator.gatewayID(index+1)
            gateway.name = "Gateway C${index+1}"
            gateway.brokerId = "C${index+1}B"


            val broker = clusterList[index].broker
            broker.id =  Generator.brokerID(index+1)
            broker.name = "Broker C${index+1}"
            broker.serverId = "C${index+1}S"


            val deviceList = clusterList[index].fogDeviceList
            deviceList.forEachIndexed {i, _ ->
                deviceList[i].controller = FogDeviceController(deviceList[i])
                deviceList[i].id = Generator.deviceID("C${index + 1}", i+1)
                deviceList[i].name = "Device${i + 1} C${index + 1}"
                deviceList[i].serverId = "C${index + 1}S"
            }
            val sensorList = clusterList[index].sensorList
            sensorList.forEachIndexed { i, sensor ->
                sensorList[i].id = Generator.sensorID("C${index + 1}", i+1)
                sensorList[i].name = "${sensor.sensorType.name} C${index + 1}"
                sensorList[i].gatewayId ="C${index + 1}G"
            }

            val server = clusterList[index].server
            server.id = Generator.serverID(index+1)
            server.name = "Server C${index+1}"
            server.deviceList = deviceList


            val cluster = Cluster(
                id = "C${index + 1}",
                name = "Cluster${index + 1}",
                server = server,
                broker = broker,
                gateway = gateway,
                fogDeviceList = deviceList,
                sensorList = sensorList
            )
            list.add(cluster)

        }
        return list
    }

    private fun generateDeviceList(clusterName: String, count: Int): MutableList<FogDevice> {
        val list = mutableListOf<FogDevice>()
        for (i in 1..count) {
            val device = FogDevice(
                id = Generator.deviceID(clusterName, i),
                name = "Device$i $clusterName",
                serverId = "${clusterName}S",
                storage = 10000.0,
                type = getFogDeviceType(isSingle = count == 1, totalCount = count, storageCount = 1)[i - 1],
                config = FogDeviceConfig(
                    db1 = 0.1,
                    db2 = 0.1,
                    db3 = 0.1,
                    db4 = 0.1,
                    db5 = 0.1,
                    architecture = "x64", os = "ubuntu", vmm = "", costPerMips = 10.0, costPerMem = 1000.0,
                    costPerStorage = 1000.0,
                    RPI = 2, flu = runDuration.toDouble() / count, tb1 = 0.1, tb2 = 0.2, tb3 = 0.3
                ),
                location = mutableListOf(Random.nextDouble(0.0, 100.0), Random.nextDouble(0.0, 100.0))
            )
            list.add(device)
        }
        return list
    }

    private fun generateSensorList(clusterName: String, count: Int, typeList: List<SensorType>): MutableList<Sensor> {
        val list = mutableListOf<Sensor>()
        val sensorTypeList = getSensorTypeList(count, typeList)
        val dataConfig = DataConfig()
        dataConfig.a1 = Random.nextDouble(0.1,0.9).roundTo(2)
        dataConfig.a2 = Random.nextDouble(0.1,0.9).roundTo(2)
        dataConfig.a3 = Random.nextDouble(0.1,0.9).roundTo(2)
        dataConfig.a4 = Random.nextDouble(0.1,0.9).roundTo(2)
        dataConfig.a5 = Random.nextDouble(0.1,0.9).roundTo(2)
        for (i in 1..count) {
            val type = sensorTypeList[i-1]
            val sensor = Sensor(
                Generator.sensorID(clusterName, i),
                "${type.name} $clusterName",
                "${clusterName}G",
                listOf(SensorCharacteristic(1000, 100, 100, 1000)),
                1000,
                Random.nextInt(6000, 12000),
                0.95,
                type,
                dataConfig = dataConfig,
                dataProcessTime = if (i % 2 == 0) 3000 else 2000
            )

            list.add(sensor)
        }
        return list
    }

    private fun getFogDeviceType(isSingle: Boolean = false, totalCount: Int, storageCount: Int): List<FogDeviceType> {
        if (isSingle) return listOf(FogDeviceType.All)
        val typeList = mutableListOf<FogDeviceType>()
        typeList.add(FogDeviceType.Warner)
        for (i in 2..totalCount - storageCount) {
            typeList.add(FogDeviceType.Analyzer)
        }
        for (i in 1..storageCount) {
            typeList.add(FogDeviceType.Storage)
        }
        return typeList
    }

    private fun getSensorTypeList(sensorCount: Int,typeList:List<SensorType>):List<SensorType> {
        val list = mutableListOf<SensorType>()
        val typeListSize = sensorCount/typeList.size
        for (i in 1..typeListSize) {
            list.addAll(typeList)
        }
        if (sensorCount%typeList.size!=0){
            list.addAll(typeList.subList(0,sensorCount%typeList.size))
        }
        return list
    }
}
