package com.fogsim.fog

import com.fogsim.fog.cluster.Cluster
import com.fogsim.fog.cluster.ClusterController
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class Application(
    val id: String,
    val name: String,
    val duration: Long,
    private val clusterList: MutableList<Cluster>
) {
    fun start() {
        State = ApplicationState.RUNNING
        LogManager.LOG_TYPE_LIST = mutableListOf(EventType.ALL)
        LogManager.logApp()

        val mainEvent = MainEvent()
        mainEvent.id = "${EventType.INITIALIZE_APP}"
        mainEvent.senderId = "${EventType.INITIALIZE_APP}"
        mainEvent.receiverId = "${EventType.INITIALIZE_APP}"
        mainEvent.data = Data(id = "", value = "0.0", sensorType = SensorType.LIGHT)
        mainEvent.date = 1234567891
        mainEvent.clusterList = clusterList
        mainEvent.eventType = EventType.INITIALIZE_APP
        EventEmitter.emit(mainEvent)
        clusterList.forEach {
            it.start()
        }
        val timer = (0..duration)
            .asSequence()
            .asFlow()
            .onEach { delay(1_000) } // specify delay


        AppCoroutineScope().launch {
            timer.collect {
                LogManager.customLog(EventType.APPLICATION, "Tick $it", sender = "Application")
                if (it == duration) {
                    State = ApplicationState.STOPPED
                    clusterList.forEach { c -> c.stop() }
                    LogManager.customLog(EventType.APPLICATION, "Application is stopped", sender = "Application")
                }
            }
        }

    }

    companion object {
        var State: ApplicationState = ApplicationState.STOPPED
        var dataPlacementType: DataPlacementType = DataPlacementType.CUSTOM_CA_REPLICA
        var taskPlacementType: TaskPlacementType = TaskPlacementType.CUSTOM_PERFORMANCE_AWARE

    }
}
