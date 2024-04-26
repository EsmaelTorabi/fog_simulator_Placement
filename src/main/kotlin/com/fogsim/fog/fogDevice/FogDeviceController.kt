package com.fogsim.fog.fogDevice

import com.fogsim.fog.EventEmitter
import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.Constants.DEFAULT_STATIC_DATA_VALUE
import com.fogsim.fog.core.models.AnalyzedData
import com.fogsim.fog.core.models.EntityStatus
import com.fogsim.fog.core.models.EventType
import com.fogsim.fog.core.utils.LocationUtils
import com.fogsim.fog.server.ServerEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class FogDeviceController(val device: FogDevice) {
    var resourceLogger = DeviceResourceLogger(device)
    var dataHandler = FogDeviceDataHandler(resourceLogger)
    private var dataAnalysis = DeviceDataAnalysis(dataHandler, resourceLogger, device)
    var queueCount = 0
    fun start() {
        device.status = EntityStatus.STARTED
        handleEvents()
    }

    fun stop() {
    }

    private fun handleEvents() {
        device.status = EntityStatus.RUNNING
        EventEmitter.event.filter { filterEvent(it) }.onEach {
            if (it is FogDeviceEvent) {
                handleDeviceEvent(it)
            } else {
                analyzeAndHandleData(it)
            }
        }.launchIn(AppCoroutineScope())


    }

    private fun sendEvent(event: MainEvent) {
        EventEmitter.emit(event)
    }

    private fun analyzeAndHandleData(event: MainEvent) {
//        LogManager.customLog(EventType.FOG_DEVICE,"device ${device.id} receive data: ${event.data}", sender = "analyzeAndHandleData ${device.id}")
        dataHandler.handleRawData(event.data)

    }

    private fun handleDeviceEvent(event: FogDeviceEvent) {
        when (event) {
            is FogDeviceEvent.RequestMissingData -> {
                // todo disable for now
                // handleRequestMissingData(event)
            }

            is FogDeviceEvent.SendWarning -> {
                //todo disable for now
                // sendWarning(event)
            }

            is FogDeviceEvent.Analyze -> {
                dataAnalysis.processRawTasks(dataHandler.getRawDataById(event.dataId))

            }

            is FogDeviceEvent.HandleAnalyzedData -> {
                handleAnalyzedData(event)
            }

            is FogDeviceEvent.SendAnalyzedData -> {

                // todo disable for now
//                debouncer.debounce(Void::class.java, {
//                    sendAnalyzedData(event)
//                }, 300, TimeUnit.MILLISECONDS)
            }

            is FogDeviceEvent.ProvideRequestedData -> {
                provideRequestedData(event)
            }

            is FogDeviceEvent.CatchRequestedData -> {
                catchRequestedData(event.requestedData)
            }

            is FogDeviceEvent.UpdateDataConfig -> {
                dataHandler.updateDataConfigs(event.dataId, event.dataConfig)
            }

            is FogDeviceEvent.UpdateDeviceConfig -> {
                updateDeviceConfig(event.deviceConfig, event.taskConfig)
            }
        }
    }


    private fun updateDeviceConfig(deviceConfig: List<Double>, taskConfig: List<Double>) {
        device.config.db1 = deviceConfig[0]
        device.config.db2 = deviceConfig[1]
        device.config.db3 = deviceConfig[2]
        device.config.db4 = deviceConfig[3]
        device.config.db5 = deviceConfig[4]
        device.config.tb1 = taskConfig[0]
        device.config.tb2 = taskConfig[1]
        device.config.tb3 = taskConfig[2]
    }

    private fun catchRequestedData(data: AnalyzedData) {
        dataHandler.handleAnalyzedData(data)
        dataAnalysis.processAnalyzedTasks(data)
        resourceLogger.reduceEmptySpace(data.value)
        resourceLogger.addDownloadCost(data.value)
    }

    private fun provideRequestedData(event: FogDeviceEvent.ProvideRequestedData) {
        val date = dataHandler.provideRequestedData(event.type)
        resourceLogger.addTransferCost(
            date?.value ?: 0.0,
            LocationUtils.getDistance(device.location, event.targetDeviceLocation)
        )
        date?.let {
            val e = FogDeviceEvent.CatchRequestedData(it)
            e.receiverId = event.targetDeviceId
            e.senderId = device.id
            e.eventType = EventType.FOG_DEVICE
            sendEvent(e)
        }
    }

    private fun sendAnalyzedData(event: FogDeviceEvent.SendAnalyzedData) {
        val e = ServerEvent.HandleAnalyzedData(event.analyzedData, device)
        resourceLogger.addUploadCost(event.analyzedData.value)
        e.senderId = device.id
        e.receiverId = device.serverId
        e.eventType = EventType.FOG_DEVICE
        sendEvent(e)
    }

    private fun handleAnalyzedData(event: FogDeviceEvent.HandleAnalyzedData) {

        dataHandler.handleAnalyzedData(event.analyzedData)
        dataAnalysis.processAnalyzedTasks(event.analyzedData)
        resourceLogger.reduceEmptySpace(event.analyzedData.value)
    }

    private fun sendWarning(event: FogDeviceEvent.SendWarning) {
        resourceLogger.addUploadCost(DEFAULT_STATIC_DATA_VALUE)
        val e = ServerEvent.HandleWarning(device.id, event.warning)
        e.receiverId = device.serverId
        e.eventType = EventType.FOG_DEVICE
        sendEvent(e)
    }

    private fun filterEvent(event: MainEvent): Boolean {
        return event.receiverId == device.id && (device.status == EntityStatus.RUNNING || event is FogDeviceEvent)
    }


}