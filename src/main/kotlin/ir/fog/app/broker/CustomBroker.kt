package ir.fog.app.broker

import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.entities.CustomTuple
import java.io.Serializable
import java.util.concurrent.TimeUnit

class CustomBroker(
    var id: String = "0",
    var name: String,
    var minValidSize: Long,
    var maxValidSize: Long,
    var minValidCpuLength: Long,
    var maxValidCpuLength: Long,
    var totalClusterSpace: Long,
    var clusterEmptySpace: Long,
    var isActive: Boolean = true,
    var delay: Long = 100
): Serializable {
    var serverId: String = ""
    private fun handleReceivedTuple(tuple: CustomTuple) {
        if (isCloudTuple(tuple)) {
            sendToCloud(tuple)
        } else {
//            sendToCurrentCluster(tuple)
            if (isTupleValid(tuple)) {
                sendToCurrentCluster(tuple)
            } else {
                findBestNextCluster()
            }
        }

    }

    fun startEntity() {

        EventBus.observe().filter { it.eventType == SimEvents.BROKER_TUPLE /*&& it.currentEntityId == id*/ }
            .doOnNext { /*print("Broker Event Received  ${Date()} \n")*/ }
            .delay(delay, TimeUnit.MILLISECONDS)
            .subscribe {
                handleReceivedTuple(it.data as CustomTuple)
            }
    }

    private fun isTupleValid(tuple: CustomTuple): Boolean {
        return tuple.cloudletFileSize in minValidSize..maxValidSize && tuple.cloudletLength in minValidCpuLength..maxValidCpuLength &&
                tuple.cloudletFileSize <= totalClusterSpace && tuple.cloudletFileSize <= clusterEmptySpace
    }

    private fun isCloudTuple(tuple: CustomTuple): Boolean {
        return tuple.isCloudTuple
    }

    private fun findBestNextCluster() {
        println("find best Cluster")
    }


    private fun sendToCurrentCluster(tuple: CustomTuple) {
        EventBus.sendEvent(
            FogEvent(
                this.name,
                0.0,
                SimEvents.SERVER_TUPLE,
                data = tuple,
                currentEntityId = id,
                nextEntity = serverId
            )
        )
    }

    private fun sendToCloud(tuple: CustomTuple) {
        println("send to cloud")
    }
}
