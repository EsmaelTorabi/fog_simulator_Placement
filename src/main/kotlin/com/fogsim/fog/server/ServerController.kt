package com.fogsim.fog.server

import com.fogsim.fog.Application
import com.fogsim.fog.EventEmitter
import com.fogsim.fog.LogManager
import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.Constants
import com.fogsim.fog.core.models.*
import com.fogsim.fog.core.utils.LocationUtils
import com.fogsim.fog.fogDevice.FogDevice
import com.fogsim.fog.fogDevice.FogDeviceEvent
import com.fogsim.fog.server.placement.RuntimeAdaption
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

class ServerController(val server: Server) {
    private val dataHandler = ServerDataHandler()
    private val serverLogger = ServerLogger(server)
    private val deviceHandler = ServerDeviceHandler(server.id, serverLogger)
    private val runtimeAdaption = RuntimeAdaption(server.id, dataHandler, deviceHandler, server.deviceList)

    fun start() {
        server.status = EntityStatus.STARTED

        handleEvents()
    }

    fun stop() {
        runtimeAdaption.stop()
    }

    private fun handleEvents() {
        server.status = EntityStatus.RUNNING
        EventEmitter.event.filter { filterEvent(it) }.onEach {
            if (it is ServerEvent) {
                handleServerEvent(it)
            } else {
                analyzeAndHandleData(it)
            }

        }.launchIn(AppCoroutineScope())

    }

    private fun analyzeAndHandleData(event: MainEvent) {
        dataHandler.analyzeData(event.data)
        if (dataHandler.dataConfigListMap.containsKey(event.data.id)) {
            event.data.config = dataHandler.dataConfigListMap[event.data.id]!!
        }
        deviceHandler.handleRawData(event.data, server.deviceList)

        deviceHandler.dataDeviceListMap[event.data.id]?.forEach { device ->
            if (device.controller!!.resourceLogger.emptySpace < 0) {
                sendCloud(event(data = event.data, receiverId = device.id))
            } else {
                serverLogger.addSensorTypeStorageCost(
                    event.data.sensorType,
                    LocationUtils.getDistance(server.location, device.location) * Constants.DEFAULT_UNIT_COST
                )
                sendEvent(event(data = event.data, receiverId = device.id))
            }
        }

        if (!runtimeAdaption.isStarted && Application.dataPlacementType == DataPlacementType.CUSTOM_CA_REPLICA) {
            runtimeAdaption.isStarted = true
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                delay(5000)
                runtimeAdaption.start()
            }
        }


    }

    private fun handleServerEvent(event: ServerEvent) {
        when (event) {
            is ServerEvent.RequestMissingData -> {
                handleRequestMissingData(event)
            }

            is ServerEvent.TransferToDevice -> {
                LogManager.customLog(
                    EventType.SERVER,
                    "Server ${server.id} **Transfer** data to device ${event.deviceId}: ${event.data}",
                    sender = "handleServerEvent ${server.id}"
                )
                sendEvent(
                    event(
                        data = event.requestedData,
                        receiverId = event.deviceId
                    )
                )
            }

            is ServerEvent.UpdateDevice -> updateDevice(event.device)
            is ServerEvent.HandleWarning -> handleWarning(event.deviceId, event.warning)
            is ServerEvent.SendTaskToDevice -> {

                //todo disabled for now
//                val e = FogDeviceEvent.Analyze
//                e.receiverId = event.deviceId
//                sendEvent(e)
            }

            is ServerEvent.HandleAnalyzedData -> {
                handleAnalyzedData(event)
            }

            ServerEvent.CheckAllTasksFinished -> checkFinishTasks()
            is ServerEvent.UpdateDataConfig -> {
                dataHandler.updateDataConfigListMap(event.dataId, event.dataConfig)
            }

            is ServerEvent.DecreaseNumberOfCopies -> {
                println("DecreaseNumberOfCopies")
                val n = event.count
                var reduceCount = if (n > (deviceHandler.dataDeviceListMap[event.data.id]?.size ?: 0)) {
                    (deviceHandler.dataDeviceListMap[event.data.id]?.size ?: 0)
                } else {
                    n
                }
                for (i in 0 until reduceCount) {
                    deviceHandler.dataDeviceListMap[event.data.id]?.removeFirstOrNull()
                }
            }

            is ServerEvent.IncreaseNumberOfCopies -> {
                println("IncreaseNumberOfCopies")
                val usedDeviceList = deviceHandler.dataDeviceListMap[event.data.id] ?: mutableListOf()
                if (usedDeviceList.isNotEmpty()) {
                    val unusedDeviceList = server.deviceList.filter { !usedDeviceList.contains(it) }
                    val n = event.count
                    if (n < unusedDeviceList.size) {
                        for (i in 0 until n) {
                            sendEvent(event(data = event.data, receiverId = unusedDeviceList[i].id))
                        }
                    } else {
                        unusedDeviceList.forEach {
                            sendEvent(event(data = event.data, receiverId = it.id))
                        }
                    }
                }
            }
        }
    }

    private fun handleRequestMissingData(event: ServerEvent.RequestMissingData) {
        val targetDeviceId = deviceHandler.getProviderDeviceId(dataHandler.getDataListForSensorType(event.type))
        targetDeviceId?.let {
            val e = FogDeviceEvent.ProvideRequestedData(event.type, event.deviceId, event.deviceLocation)
            e.receiverId = targetDeviceId
            sendEvent(e)
        }
    }

    private fun handleAnalyzedData(event: ServerEvent.HandleAnalyzedData) {
        serverLogger.addSensorTypeRuntime(event.analyzedData.sensorType, event.analyzedData.runtime)
        dataHandler.handleAnalyzedData(event.analyzedData)
        deviceHandler.handleAnalyzingData(event.analyzedData, event.analyzerDevice)
        val e = FogDeviceEvent.HandleAnalyzedData(event.analyzedData)
        e.receiverId = server.deviceList.first { it.type == FogDeviceType.Warner }.id

        sendEvent(e)

    }

    private fun handleWarning(deviceId: String, warning: Warning) {
        LogManager.customLog(EventType.SERVER, "device $deviceId: $warning", sender = "handleWarning ${server.id}")

    }

    private fun checkFinishTasks() {
        if (Application.State == ApplicationState.STOPPED) {
            var isFinished = true
            for (device in server.deviceList) {
                if (device.controller!!.resourceLogger.queueCount > 0) {
                    isFinished = false
                    break
                }
            }
            if (isFinished) {
                serverLogger.logAllResults(server.deviceList)
            }
        }
    }

    private fun updateDevice(device: FogDevice) {
        deviceHandler.updateDevice(device)
        // update device in server
        for (i in server.deviceList.indices) {
            if (server.deviceList[i].id == device.id) {
                server.deviceList[i] = device
                break
            }
        }
    }

    private fun sendEvent(event: MainEvent) {
        event.senderId = server.id
        event.eventType = EventType.SERVER
        EventEmitter.emit(event)
    }

    private fun sendCloud(event: MainEvent) {
        LogManager.customLog(
            EventType.SERVER,
            "Server ${server.id} send data to cloud: ${event.data}",
            sender = "sendCloud ${server.id}"
        )
    }

    private fun filterEvent(event: MainEvent): Boolean {
        return event.receiverId == server.id && (server.status == EntityStatus.RUNNING || event is ServerEvent)
    }


    private fun event(
        id: String = server.id,
        sederId: String = server.id,
        receiverId: String = server.id,
        data: Data,
        date: Long = Date().time
    ): MainEvent {
        val mainEvent = MainEvent()
        mainEvent.id = id
        mainEvent.senderId = sederId
        mainEvent.receiverId = receiverId
        mainEvent.data = data
        mainEvent.date = date
        return mainEvent
    }
}
