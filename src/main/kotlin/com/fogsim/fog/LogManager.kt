package com.fogsim.fog

import com.fogsim.AppResult
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.EventType
import com.fogsim.fog.core.models.SensorType
import com.fogsim.fog.fogDevice.FogDeviceEvent
import com.fogsim.fog.server.ServerEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object LogManager {
    var LOG_TYPE_LIST = mutableListOf<EventType>()
    fun logApp() {
        EventEmitter.event.onEach {
            printLogs(it)
        }.launchIn(AppCoroutineScope())
    }

    fun logResults(
        runtime: Long = 0,
        storageCost: Double = 0.0,
        transferCost: Double = 0.0,
        uploadCost: Double = 0.0,
        downloadCost: Double = 0.0,
        energyCost: Double = 0.0,
        availability:List<Pair<String, Double>> = listOf(),
        accessibility:List<Pair<String, Double>> = listOf(),
        totalCpuTime:Double = 0.0,
        networkCost : Double = 0.0
    ) {
        var result = AppResult(
            runtime = runtime.toDouble(),
            storageCost = storageCost,
            transferCost = transferCost,
            uploadCost = uploadCost,
            downloadCost = downloadCost,
            energyCost = energyCost,
            availability = availability.map { it.second }.average(),
            accessibility = accessibility.map { it.second }.average(),
            totalCpuTime = totalCpuTime,
            networkCost = networkCost
        )
        val mainEvent = MainEvent()
        mainEvent.id = ""
        mainEvent.senderId = ""
        mainEvent.receiverId = ""
        mainEvent.data = Data(id = "", value = "0.0", sensorType = SensorType.LIGHT)
        mainEvent.date = 1234567891
        mainEvent.eventType = EventType.RESULT
        mainEvent.result = result
        EventEmitter.emit(mainEvent)


        println("Results:")
        println("Runtime: $runtime")
        println("Storage Cost: $storageCost")
        println("Transfer Cost: $transferCost")
        println("Upload Time: $uploadCost")
        println("Download Time: $downloadCost")
        println("Energy Cost: $energyCost")
        println("Availability: $availability")
        println("Total avrage Availability: ${availability.map { it.second }.average()}")
        println("Accessibility: $accessibility")
        println("Total avrage Accessibility: ${accessibility.map { it.second }.average()}")
        println("Total Delay: ${downloadCost + uploadCost}")
        println("Total CPU Cost: $totalCpuTime")
        println("Total Network Cost: $networkCost")


    }


    fun customLog(eventType: EventType, message: String, sender: String) {
        if ((LOG_TYPE_LIST.contains(eventType) || LOG_TYPE_LIST.contains(EventType.ALL)) && LOG_TYPE_LIST.contains(
                EventType.CUSTOM
            )
        ) {
            println("\uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1  \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1  \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1  \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1")
            println(
                "\uD83D\uDD16 Event: $eventType \n" + "\uD83D\uDD16 Event: $sender \n" + "\uD83D\uDD16 Message: $message" +
                        "\n" +
                        "\uD83D\uDD16 Time: ${LocalDateTime.now()}"
            )
        }
    }


    private fun printLogs(event: MainEvent) {
        if (LOG_TYPE_LIST.contains(event.eventType) || LOG_TYPE_LIST.contains(EventType.ALL)) {
            println("\uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1  \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1  \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1  \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1 \uD83E\uDDF1")
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            val instant = Instant.ofEpochMilli(event.date)
            val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

            println("\uD83D\uDD16 Event: ${event.eventType}")
            println("\uD83D\uDD16 Sender ID: ${event.senderId}")
            println("\uD83D\uDD16 Receiver ID: ${event.receiverId}")
            println("\uD83D\uDD16 Data: ${event.data}")
            if (event is FogDeviceEvent) {
                when (event) {
                    is FogDeviceEvent.CatchRequestedData -> {
                        println("\uD83D\uDD16 Catch Requested Data")
                        println("\uD83D\uDD16 Event type: CatchRequestedData")
                        println("\uD83D\uDD16 Analyzed Data ID: ${event.requestedData.id}")
                        println("\uD83D\uDD16 Analyzed Data Value: ${event.requestedData.value}")
                        println("\uD83D\uDD16 Analyzed Data Type: ${event.requestedData.sensorType}")
                        println("\uD83D\uDD16 Inner Data: ${event.requestedData.data}")
                    }

                    is FogDeviceEvent.HandleAnalyzedData -> {
                        println("\uD83D\uDD16 Handle Analyzed Data")
                        println("\uD83D\uDD16 Event type: HandleAnalyzedData")
                        println("\uD83D\uDD16 Analyzed Data ID: ${event.analyzedData.id}")
                        println("\uD83D\uDD16 Analyzed Data Value: ${event.analyzedData.value}")
                        println("\uD83D\uDD16 Analyzed Data Type: ${event.analyzedData.sensorType}")
                        println("\uD83D\uDD16 Inner Data: ${event.analyzedData.data}")
                    }

                    is FogDeviceEvent.ProvideRequestedData -> println("\uD83D\uDD16 Provide Requested Data - Target Device ID: ${event.targetDeviceId}")
                    is FogDeviceEvent.RequestMissingData -> println("\uD83D\uDD16 Request Missing Data: ${event.type}")
                    is FogDeviceEvent.SendAnalyzedData -> {
                        println("\uD83D\uDD16 Send Analyzed Data: ${event.analyzedData}")
                        println("\uD83D\uDD16 Event type: SendAnalyzedData")
                        println("\uD83D\uDD16 Analyzed Data ID: ${event.analyzedData.id}")
                        println("\uD83D\uDD16 Analyzed Data Value: ${event.analyzedData.value}")
                        println("\uD83D\uDD16 Analyzed Data Type: ${event.analyzedData.sensorType}")
                        println("\uD83D\uDD16 Inner Data: ${event.analyzedData.data}")
                    }

                    is FogDeviceEvent.SendWarning -> println("\uD83D\uDD16 Send Warning: ${event.warning}")
                    is FogDeviceEvent.Analyze -> println("\uD83D\uDD16 Start Analysis")
                    is FogDeviceEvent.UpdateDataConfig -> println("")
                    is FogDeviceEvent.UpdateDeviceConfig -> println("")
                }
            }
            if (event is ServerEvent) {
                when (event) {
                    is ServerEvent.HandleAnalyzedData -> {
                        println("\uD83D\uDD16 Received Analyzed Data")
                        println("\uD83D\uDD16 Analyzer Device ID: ${event.analyzerDevice.id}")
                        println("\uD83D\uDD16 Analyzed Data ID: ${event.analyzedData.id}")
                        println("\uD83D\uDD16 Analyzed Data Value: ${event.analyzedData.value}")
                        println("\uD83D\uDD16 Analyzed Data Type: ${event.analyzedData.sensorType}")
                        println("\uD83D\uDD16 Inner Data: ${event.analyzedData.data}")
                    }

                    is ServerEvent.HandleWarning -> println("\uD83D\uDD16 Handle Warning: ${event.warning}")
                    is ServerEvent.RequestMissingData -> println("\uD83D\uDD16 Request Missing Data: ${event.type}")
                    is ServerEvent.SendTaskToDevice -> println("\uD83D\uDD16 Send Task To Device: ${event.deviceId}")
                    is ServerEvent.TransferToDevice -> println("\uD83D\uDD16 Transfer To Device: ${event.deviceId}")
                    is ServerEvent.UpdateDevice -> println("\uD83D\uDD16 Update Device: ${event.device}")
                    ServerEvent.CheckAllTasksFinished -> println("\uD83D\uDD16 Check All Tasks Finished")
                    is ServerEvent.UpdateDataConfig -> println("")
                    is ServerEvent.DecreaseNumberOfCopies -> println("")
                    is ServerEvent.IncreaseNumberOfCopies -> println("")
                }
            }

            println("\uD83D\uDD16 Time: ${date.format(formatter)}")

        }
    }


}
