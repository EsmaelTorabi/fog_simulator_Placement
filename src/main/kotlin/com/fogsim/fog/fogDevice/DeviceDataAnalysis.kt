package com.fogsim.fog.fogDevice

import com.fogsim.fog.EventEmitter
import com.fogsim.fog.LogManager
import com.fogsim.fog.core.Constants
import com.fogsim.fog.core.models.*
import com.fogsim.fog.core.utils.DataUtils
import com.fogsim.fog.server.ServerEvent
import java.util.*

class DeviceDataAnalysis(
    private var dataHandler: FogDeviceDataHandler,
    private val resourceLogger: DeviceResourceLogger,
    private val device: FogDevice
) {
    private val fireSigns = mutableListOf(SensorType.SMOKE, SensorType.TEMPERATURE, SensorType.GAS)
    private val fireInfo = mutableListOf<Pair<SensorType, AnalyzedData>>()
    private val earthquakeSigns = mutableListOf(SensorType.ACCELEROMETER, SensorType.GYROSCOPE, SensorType.MAGNETOMETER)
    private val earthquakeInfo = mutableListOf<Pair<SensorType, AnalyzedData>>()
    private val stormSigns = mutableListOf(SensorType.TEMPERATURE, SensorType.PRESSURE, SensorType.HUMIDITY)
    private val stormInfo = mutableListOf<Pair<SensorType, AnalyzedData>>()


    var rawTasksQueue = mutableListOf<Task>()
    private var analyzedTasksQueue = mutableListOf<Task>()
    private var isBusy = false
    private var isAnalyzing = false


    fun processRawTasks(data: Data) {
        rawTasksQueue.add(Task(data.processTime, data))
        resourceLogger.increaseCpuCost(data.processTime/1000 * device.config.costPerMips)
        dataHandler.changeDataAnalysisState(data, TaskAnalyzeState.ANALYZING)
        if (!isBusy)
            analyzeData()
    }

    fun processAnalyzedTasks(data: AnalyzedData) {
        analyzedTasksQueue.add(Task(100, data.data, data))
        resourceLogger.increaseCpuCost(0.1 * device.config.costPerMips)
        if (!isAnalyzing)
            analyzeWarning()
    }

    private fun analyzeData() {
        isBusy = true


        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                val data = rawTasksQueue.firstOrNull()?.data ?: return

                dataHandler.changeDataAnalysisState(data, TaskAnalyzeState.ANALYZED)
                val analyzedData = AnalyzedData(
                    id = data.id,
                    sensorType = data.sensorType,
                    size = DataUtils.dataSizeInBytes(data.value).toDouble(),
                    data = data,
                    value= DataUtils.binaryToDecimal(data.value).toDouble(),
                    runtime = System.currentTimeMillis() - rawTasksQueue.first().createdAt,
                )
                LogManager.customLog(
                    EventType.FOG_DEVICE,
                    "device ${device.id} Analyzed data: $analyzedData",
                    sender = "${device.id} \n receiver:${device.serverId}"
                )

                dataHandler.handleAnalyzedData(analyzedData)

                val event = ServerEvent.HandleAnalyzedData(analyzedData, device)
                event.receiverId = device.serverId
                event.senderId = device.id
                event.eventType = EventType.FOG_DEVICE
                EventEmitter.emit(event)

                resourceLogger.addUploadCost(event.analyzedData.size)


                resourceLogger.addBusyTime((rawTasksQueue.firstOrNull()?.duration?.toDouble() ?: 0.0))
                rawTasksQueue.removeFirstOrNull()
                resourceLogger.queueCount = rawTasksQueue.size
                println("device ${device.id} Analyzed Data Queue Size: ${rawTasksQueue.size}")
                if (rawTasksQueue.isNotEmpty())
                    analyzeData()
                else {
                    val e = ServerEvent.CheckAllTasksFinished
                    e.receiverId = device.serverId
                    e.senderId = device.id
                    e.eventType = EventType.SERVER
                    EventEmitter.emit(e)
                    isBusy = false
                }


                t.cancel()
            }
        }, rawTasksQueue.firstOrNull()?.duration ?: 0)


    }

    private fun analyzeWarning() {
        isAnalyzing = true
        LogManager.customLog(
            EventType.FOG_DEVICE,
            "device ${device.id} Analyzed Data Queue Size: ${analyzedTasksQueue.size}",
            sender = "DeviceDataAnalysis ${device.id}"
        )
        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                val data = analyzedTasksQueue.first().analyzedData!!
                if (fireSigns.contains(data.sensorType)) {
                    try {
                        fireInfo.removeIf { it.first == data.sensorType }
                    } catch (e: Exception) {
                        LogManager.customLog(
                            EventType.FOG_DEVICE,
                            "error in device ${device.id}: ${e.message}",
                            sender = "DeviceDataAnalysis2 ${device.id}"
                        )
                    }

                    fireInfo.add(Pair(data.sensorType, data))
                }
                if (earthquakeSigns.contains(data.sensorType)) {
                    try {
                        earthquakeInfo.removeIf { it.first == data.sensorType }
                    } catch (e: Exception) {
                        LogManager.customLog(
                            EventType.FOG_DEVICE,
                            "error in device ${device.id}: ${e.message}",
                            sender = "DeviceDataAnalysis3 ${device.id}"
                        )
                    }
                    earthquakeInfo.add(Pair(data.sensorType, data))
                }
                if (stormSigns.contains(data.sensorType)) {
                    try {
                        stormInfo.removeIf { it.first == data.sensorType }
                    } catch (e: Exception) {
                        LogManager.customLog(
                            EventType.FOG_DEVICE,
                            "error in device ${device.id}: ${e.message}",
                            sender = "DeviceDataAnalysis4 ${device.id}"
                        )
                    }
                    stormInfo.add(Pair(data.sensorType, data))
                }

                if (fireInfo.size == fireSigns.size) {
                    val fireWarning = DeviceScenarios.fireWarning(fireInfo.map { it.second })
                    resourceLogger.addUploadCost(Constants.DEFAULT_STATIC_DATA_VALUE)
                    if (fireWarning.level != WarningLevel.NONE) {
                        val e = ServerEvent.HandleWarning(device.id, fireWarning)
                        e.receiverId = device.id
                        e.senderId = device.id
                        e.eventType = EventType.FOG_DEVICE
                        EventEmitter.emit(e)
                    }



                    // fireInfo.forEach { dataHandler.removeAnalyzedData(it.second) }
                    fireInfo.clear()
                } else if (fireInfo.size == fireSigns.size - 1) {
                    // get the missing sensor type
                    val missingSensorType = fireSigns.filter { !fireInfo.map { i -> i.first }.contains(it) }[0]
                    LogManager.customLog(
                        EventType.FOG_DEVICE,
                        "device ${device.id} request missing data: $missingSensorType",
                        sender = "handleRequestMissingData ${device.id}"
                    )
                    resourceLogger.addUploadCost(Constants.DEFAULT_STATIC_DATA_VALUE)
                    // todo check queue before requesting for missing data
                    val e = ServerEvent.RequestMissingData(missingSensorType, device.id, device.location)
                    e.receiverId = device.serverId
                    e.senderId = device.id
                    e.eventType = EventType.FOG_DEVICE
                    EventEmitter.emit(e)
                    /// request missing sensor type data
                }

                if (earthquakeInfo.size == earthquakeSigns.size) {
                    val earthquakeWarning = DeviceScenarios.earthquakeWarning(earthquakeInfo.map { it.second })
                    if (earthquakeWarning.level != WarningLevel.NONE) {
                        val event = FogDeviceEvent.SendWarning(earthquakeWarning)
                        event.receiverId = device.id
                        event.senderId = device.id
                        event.eventType = EventType.FOG_DEVICE
                        EventEmitter.emit(event)
                    }

                    // earthquakeInfo.forEach { dataHandler.removeAnalyzedData(it.second) }
                    earthquakeInfo.clear()
                } else if (earthquakeInfo.size == earthquakeSigns.size - 1) {
                    val missingSensorType =
                        earthquakeSigns.filter { !earthquakeInfo.map { i -> i.first }.contains(it) }[0]
                    val event = FogDeviceEvent.RequestMissingData(missingSensorType)
                    event.receiverId = device.id
                    event.senderId = device.id
                    event.eventType = EventType.FOG_DEVICE
                    EventEmitter.emit(event)
                }

                if (stormInfo.size == stormSigns.size) {
                    val stormWarning = DeviceScenarios.stormWarning(stormInfo.map { it.second })
                    if (stormWarning.level != WarningLevel.NONE) {
                        val event = FogDeviceEvent.SendWarning(stormWarning)
                        event.receiverId = device.id
                        event.senderId = device.id
                        event.eventType = EventType.FOG_DEVICE
                        EventEmitter.emit(event)
                    }

                    // stormInfo.forEach { dataHandler.removeAnalyzedData(it.second) }
                    stormInfo.clear()
                } else if (stormInfo.size == stormSigns.size - 1) {
                    val missingSensorType = stormSigns.filter { !stormInfo.map { i -> i.first }.contains(it) }[0]
                    val event = FogDeviceEvent.RequestMissingData(missingSensorType)
                    event.receiverId = device.id
                    event.senderId = device.id
                    event.eventType = EventType.FOG_DEVICE
                    EventEmitter.emit(event)
                }

                analyzedTasksQueue.removeFirstOrNull()
                println("analyzedTasksQueue size: ${analyzedTasksQueue.size}")
                resourceLogger.queueCount = analyzedTasksQueue.size
                if (analyzedTasksQueue.isNotEmpty())
                    analyzeWarning()
                else {
                    val event = ServerEvent.CheckAllTasksFinished
                    event.receiverId = device.serverId
                    event.senderId = device.id
                    event.eventType = EventType.SERVER
                    EventEmitter.emit(event)
                    isAnalyzing = false
                }

                t.cancel()

            }
        }, analyzedTasksQueue.firstOrNull()?.duration ?: 1000)

    }
}

