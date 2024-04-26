# [Log Manager](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/LogManager.kt)

LogManager is singleton and Centralized, and it is responsible for logging the results of the simulation.
LogManager has a list of Loggers, and it is responsible for calling the log method of each logger in the list.
with LogManager we don't need to pass the logger to each class, we just need to call the log method of LogManager and it will call the log method of each logger in the list.
we use Log Manager as Object because we don't need to create more than one instance of it.

## Variables
```kotlin
var LOG_TYPE_LIST = mutableListOf<EventType>()
```
This variable is a list of the types of events that we want to log.

### Log Types
```kotlin
enum class EventType {
    ALL,APPLICATION,CLUSTER,SENSOR,FOG_DEVICE,SERVER,BROKER,GATEWAY,NONE,CUSTOM,RuntimeAdaption
}
```
| Log Type | Description                                                                    |
| --- |--------------------------------------------------------------------------------|
| ALL | Log all types of events when we add this we don't need to add other log types. |
| APPLICATION | Log Application events. |
| CLUSTER | Log Cluster events. |
| SENSOR | Log Sensor events. |
| FOG_DEVICE | Log Fog Device events. |
| SERVER | Log Server events. |
| BROKER | Log Broker events. |
| GATEWAY | Log Gateway events. |
| NONE | Don't log any events. |
| CUSTOM | Log custom events. |
| RuntimeAdaption | Log Runtime Adaption events. |

- **note**: we can add more types of events if we want.
- **note**: because we use the enum class we can't add the type of events in runtime, we need to add it in the code.
- **note**: we can add more than one type of events, for example, we can add APPLICATION and SENSOR, and it will log only the events of these two types.

Example of this log type list:
```kotlin
LogManager.LOG_TYPE_LIST = mutableListOf(EventType.APPLICATION, EventType.CUSTOM)
```

## Methods

```kotlin
fun logApp() {
        EventEmitter.event.onEach {
            printLogs(it)
        }.launchIn(AppCoroutineScope())
    }
```

when we want to start the logging of the Event Emitter we call this method.
with this function we listen to the events of the Event Emitter and call the printLogs method to log the events.

```kotlin
fun logResults(
        runtime: Long = 0,
        storageCost: Double = 0.0,
        transferCost: Double = 0.0,
        uploadCost: Double = 0.0,
        downloadCost: Double = 0.0,
        energyCost: Double = 0.0
    ) {

        println("Results:")
        println("Runtime: $runtime")
        println("Storage Cost: $storageCost")
        println("Transfer Cost: $transferCost")
        println("Upload Cost: $uploadCost")
        println("Download Cost: $downloadCost")
        println("Energy Cost: $energyCost")


    }
```
this function is called by the server when the simulation is finished, and it is responsible for printing the results of the simulation.

```kotlin
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
```

this function is called by the classes when they want to log a custom event. it takes the type of the event, the message, and the sender of the event.
it checks if the type of the event is in the log type list, and if it is in the list it will print the event.

for example, if we want to log a custom event in the application class we call this function like this:
```kotlin
LogManager.customLog(EventType.APPLICATION, "Application is created", "Application")
```

```kotlin
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
                    is FogDeviceEvent.UpdateDataConfig -> TODO()
                    is FogDeviceEvent.UpdateDeviceConfig -> TODO()
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
                    is ServerEvent.UpdateDataConfig -> TODO()
                    is ServerEvent.DecreaseNumberOfCopies -> TODO()
                    is ServerEvent.IncreaseNumberOfCopies -> TODO()
                }
            }

            println("\uD83D\uDD16 Time: ${date.format(formatter)}")

        }
    }
```
in this function we log and personalize the events based on their type and data.

```kotlin
 if (LOG_TYPE_LIST.contains(event.eventType) || LOG_TYPE_LIST.contains(EventType.ALL)) {
 }
```
at first, we check if the event type is in the list of the events that we want to log or not.

if it is, we print the event type, sender id, receiver id, data and date of the event.

```kotlin
if (event is FogDeviceEvent) {
}
```

then we check if the event is a fog device event.
if it is a fog device event, we check the type of the event and print the data based on the type of the event.

```kotlin
if (event is ServerEvent) {
}

```
if it is a server event, we check the type of the event and print the data based on the type of the event.


