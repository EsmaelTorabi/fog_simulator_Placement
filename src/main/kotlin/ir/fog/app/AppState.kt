package ir.fog.app

import ir.fog.app.device.CustomFogDevice
import ir.fog.app.sensor.CustomSensor
import ir.fog.entities.CustomTuple
import ir.fog.app.gateway.Gateway
import ir.fog.app.server.Server

/**
 * @author mohsen on 1/13/23
 */
data class AppState(
    var isStarted: Boolean = false,
    var isStopped: Boolean = false,
    var isFinished: Boolean = false,
    var isPaused: Boolean = false,
    var isResumed: Boolean = false,
    var simulationTime: Int = 30,
    var availableDeviceList: MutableList<CustomFogDevice> = mutableListOf(),
    var tupleList: MutableList<CustomTuple> = mutableListOf(),
    var sensorList: MutableList<CustomSensor> = mutableListOf(),
    var gateway: Gateway? = null,
    var server: Server? = null

) {
    companion object {
        val initial = AppState()
    }

    fun build(block: Builder.() -> Unit) = Builder(this).apply(block).build()


    class Builder(state: AppState) {
        var isStarted: Boolean = state.isStarted
        var isStopped: Boolean = state.isStopped
        var isFinished: Boolean = state.isFinished
        var isPaused: Boolean = state.isPaused
        var isResumed: Boolean = state.isResumed
        var simulationTime: Int = state.simulationTime
        var availableDeviceList: MutableList<CustomFogDevice> = state.availableDeviceList
        var tupleList: MutableList<CustomTuple> = state.tupleList
        var sensorList: MutableList<CustomSensor> = state.sensorList
        var gateway: Gateway? = state.gateway
        var server: Server? = state.server


        fun build() = AppState(
            isStarted = isStarted,
            isStopped = isStopped,
            isFinished = isFinished,
            isPaused = isPaused,
            isResumed = isResumed,
            simulationTime = simulationTime,
            availableDeviceList = availableDeviceList,
            tupleList = tupleList,
            sensorList = sensorList,
            gateway = gateway,
            server = server

        )
    }
}
