package com.fogsim.fog.cluster

import com.fogsim.fog.broker.BrokerController
import com.fogsim.fog.core.models.EntityStatus
import com.fogsim.fog.fogDevice.FogDeviceController
import com.fogsim.fog.gateway.GatewayController
import com.fogsim.fog.sensor.SensorController
import com.fogsim.fog.server.ServerController
import kotlinx.coroutines.*

class ClusterController(private val cluster: Cluster) {
    fun start() {
        cluster.status = EntityStatus.STARTED
        startEntities()
    }

    fun stop() {
        cluster.status = EntityStatus.STOPPED
        stopEntities()
    }

    fun reset() {
        cluster.status = EntityStatus.PAUSED
        stopEntities()
        startEntities()
    }

    fun getTotalStorage(): Double {
        var storage = 0.0
        cluster.fogDeviceList.forEach { fogDevice ->
            storage += fogDevice.storage
        }
        return storage
    }

    fun getEmptyStorage(): Double {
        var storage = 0.0
        cluster.fogDeviceList.forEach { fogDevice ->
            storage += fogDevice.controller?.resourceLogger?.emptySpace ?: 0.0
        }
        return storage
    }

    private fun startEntities() {
        cluster.status = EntityStatus.RUNNING
        cluster.server.start()
        cluster.broker.start()
        cluster.gateway.start()

        cluster.fogDeviceList.forEach { fogDevice ->
            fogDevice.start()
        }
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            delay(3000)
            cluster.sensorList.forEach { sensor ->
                sensor.start()
            }
        }

    }

    private fun stopEntities() {
        cluster.server.stop()
        cluster.broker.stop()
        cluster.gateway.stop()

        cluster.sensorList.forEach { sensor ->
            sensor.stop()
        }
    }
}
