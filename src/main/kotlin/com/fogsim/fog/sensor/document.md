# Sensor

## Sensor Classes
![image](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/statics/sensor/sensor.png)

| Class        | Usage           |
| ------------- |:-------------:|
| [Sensor.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/Sensor.kt)      |  base class of Sensor |
| [SensorCharacteristic](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/SensorCharacteristic.kt)      | all attributes about sensor hardware.      |
| [SensorController](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/SensorController.kt) | Heart of sensor all actions happen in here      |
| [SensorEvent](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/SensorEvent.kt) | Sensor event that used in Event Emmiter     |

---

### [Sensor.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/Sensor.kt) 

this class is the base class of the sensor entity when we want to create a sensor entity we need to create a new instance of this class.

| Variable        | Usage           |
| ------------- |:-------------:|
| id |  id of the sensor. must be unique  |
| name |   name of the sensor. |
| gatewayId |  id of the target gateway that the sensor will send data  |
| characteristics |  list of sensor hardware attributes |
| latency |  the delay that we want to happen before sending the event in the sensor  |
| frequency |  interval time of sending event. **must be millisecond**  |
| availability |  availability of the sensor. **must be between 0 and 1**  |
| sensorType | type of the sensor. **must be selected from sensor types**. ex: Sound  |
| dataConfig | configs of the data that this sensor will generate and send|

#### Functions

```kotlin
override fun start() {
        controller.start()
    }
```
when we want to start the sensor we call this function

```kotlin
override fun stop() {
        controller.stop()
    }
```
when we want to stop the sensor we use this function

```kotlin
override fun reset() {
        controller.stop()
        controller.start()
    }
```
when we want to reset the sensor we use this function

---

### [SensorController.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/SensorController.kt) 

Heart of sensor all actions happen in here. for example in this class we send data. we generate data. time frequency is initialized here.
controls data entry.

```kotlin
init {
        handleEvents()
    }
```
this is the initializer and constructor of the controller. this function is called just once when the controller is created. inside this function, we call **hanleEvents** that handles events. 

```kotlin
 private var job: Deferred<Unit>? = null
```
this variable is for creating a timer in Kotlin. with this variable, we can start or stop the timer.


```kotlin
 fun start() {
        initTimer(sensor.frequency.toLong())
    }
```
this function is called when the sensor starts. inside this function, we call the initiate timer that starts the timer.

```kotlin
fun stop() {
        stopTimer()
    }
```
this function is called when the sensor stops. inside this function, we stop the timer to prevent sending data after stopping the sensor.

```kotlin
private fun initTimer(period: Long) {
        job = AppCoroutineScope().launchPeriodicAsync(period) {
            val availability = Random.nextInt(0, 1)
            if (availability < sensor.availability) {
                sendEvent(
                    event(
                        data = Data(
                            id = sensor.id,
                            value = Random.nextInt(10, 8000).toString(2),
                            sensorType = sensor.sensorType,
                            config = sensor.dataConfig
                        ),
                        receiverId = sensor.gatewayId
                    )
                )
            }

        }
    }
```
this function is for starting the timer.
```kotlin
val availability = Random.nextInt(0, 1)
```
in this function, we generate a random number between 0 and 1 to check the availability. 0 means 0% and 1 means 100% success.

```kotlin
if (availability < sensor.availability) {}
```
if the generated number is less than sensor availability it means success else failure.
for example :
if sensor availability is 0.8 in 80% of the time sensor works correctly.

```kotlin
sendEvent(
                    event(
                        data = Data(
                            id = sensor.id,
                            value = Random.nextInt(10, 8000).toString(2),
                            sensorType = sensor.sensorType,
                            config = sensor.dataConfig
                        ),
                        receiverId = sensor.gatewayId
                    )
```

if availability succeeds then we generate an event and send it to the gateway.



```kotlin
private fun sendEvent(event: MainEvent) {
        EventEmitter.emit(event)
    }
```
in this function, we send events via EventEmitter to the other entities. **Attention!!** we can not send direct events to other entities.



```kotlin
private fun stopTimer() {
        job?.cancel()
    }
```

this function is for stopping the the Timer when sensor stops.

```kotlin
private fun handleEvents() {
        EventEmitter.event.filter { it.receiverId == sensor.id }.onEach {
            when (it) {
                is SensorEvent.SendData -> {
                    val event = event(receiverId = sensor.gatewayId, data = it.data)
                    sendEvent(event)
                }
            }
        }.launchIn(AppCoroutineScope())
    }
```
this function is for receiving EventEmitter events inside the sensor.
because the sensor event class has just one event we have just one event inside when. that sends data to the sensor itself.

```kotlin
private fun event(
        id: String = sensor.id,
        sederId: String = sensor.id,
        receiverId: String = "",
        data: Data = Data(sensor.id, Random.nextInt(0, 100).toString(2), sensor.sensorType),
        date: Long = Date().time
    ): MainEvent {
        val mainEvent = MainEvent()
        mainEvent.id = id
        mainEvent.senderId = sederId
        mainEvent.receiverId = receiverId
        mainEvent.data = data
        mainEvent.date = date
        mainEvent.eventType = EventType.SENSOR
        return mainEvent
    }
```

this function is for generating the event to send in the **sendEvent** Function.

```kotlin
id: String = sensor.id,
        sederId: String = sensor.id,
        receiverId: String = "",
        data: Data = Data(sensor.id, Random.nextInt(0, 100).toString(2), sensor.sensorType),
        date: Long = Date().time
```

**id**: id is the id of the event. mainly we put the sender id on it.
**sederId**: id of the sender in this class it is the sensor id.
**receiverId**: id of the entity that we want to send data to.
**data**: the data that we want to send to the target entity. because is sensor value of the data is **binary** **0,1**
**date**: time that we created this event

---

### [SensorEvent.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/sensor/SensorEvent.kt) 

This class is a collection of sensor events.
in this class, we add sensor-specific events.

```kotlin
sealed class SensorEvent : MainEvent() {
    object SendData : SensorEvent()
}
```
**SendData** 
this event is used in the sensor Controller for sending the data to the sensor itself.


