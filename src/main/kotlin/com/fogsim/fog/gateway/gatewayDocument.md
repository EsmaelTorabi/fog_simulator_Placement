# Gateway

## Gateway Classes
![image](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/statics/gateway/gateway.png)

| Class        | Usage           |
| ------------- |:-------------:|
| [Gateway](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/gateway/Gateway.kt)      |  base class of Gateway |
| [GatewayController](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/gateway/GatewayController.kt) | Heart of Gateway. all actions happen in here      |
| [GatewayEvent](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/gateway/GatewayEvent.kt) | Gateway event that used in Event Emmiter     |

---

### [Gateway.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/gateway/Gateway.kt) 

this class is the base class of the gateway entity when we want to create a gateway entity we need to create a new instance of this class.

| Variable        | Usage           |
| ------------- |:-------------:|
| id |  id of the gateway. must be unique  |
| name |   name of the gateway. |
| brokerId |  id of the target broker that the gateway will send data  |
| delay |  the delay that we want to happen before sending the event in the gateway  |

#### Functions

```kotlin
override fun start() {
        controller.start()
    }
```
when we want to start the gateway we call this function

```kotlin
override fun stop() {
        controller.stop()
    }
```
when we want to stop the gateway we use this function

```kotlin
override fun reset() {
        controller.stop()
        controller.start()
    }
```
when we want to reset the gateway we use this function

---

### [GatewayController.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/gateway/GatewayController.kt)

Heart of gateway. all actions happen in here. for example in this class we send data. we generate data. time frequency is initialized here.
controls data entry.

```kotlin
 fun start() {
        handleEvents()
    }
```
this function is called when the gateway starts. inside this function, we call **hanleEvents** that handles events. 

```kotlin
fun stop() {
        
    }
```
this function is called when the gateway stops. inside this function, we stop the controller.




```kotlin
private fun sendEvent(event: MainEvent) {
        EventEmitter.emit(event)
    }
```
in this function, we send events via EventEmitter to the other entities. **Attention!!** We can not send direct events to other entities.



```kotlin
private fun handleEvents() {
        EventEmitter.event.filter { it.receiverId == gateway.id }.onEach {
            sendEvent(event(data = it.data, receiverId = gateway.brokerId))
        }.launchIn(AppCoroutineScope())
    }
```
this function is for receiving EventEmitter events inside the gateway. 

```kotlin
EventEmitter.event.filter { it.receiverId == gateway.id }
```
in every entity at first, we check the **receiver id**. if the id is the same gateway id we let the event continue.




```kotlin
private fun event(
        id: String = gateway.id,
        sederId: String = gateway.id,
        receiverId: String = "",
        data: Data,
        date: Long = Date().time
    ):MainEvent {
        val mainEvent = MainEvent()
        mainEvent.id = id
        mainEvent.senderId = sederId
        mainEvent.receiverId = receiverId
        mainEvent.data = data
        mainEvent.date = date
        mainEvent.eventType = EventType.GATEWAY
        return mainEvent
    }
```

this function is for generating the event to send in the **sendEvent** Function.

```kotlin
private fun event(
        id: String = gateway.id,
        sederId: String = gateway.id,
        receiverId: String = "",
        data: Data,
        date: Long = Date().time
    )
```

**id**: id is the id of the event. mainly we put the sender id on it.
**sederId**: id of the sender in this class it is the gateway id.
**receiverId**: id of the entity that we want to send data to in here its broker id.
**data**: the data that we want to send to the target entity. because is gateway value of the data is **binary** **0,1**
**date**: time that we created this event

---

### [GatewayEvent.Kt](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/gateway/GatewayEvent.kt) 

This class is a collection of gateway events.
in this class, we add gateway-specific events.

```kotlin
sealed class GatewayEvent : MainEvent() {
    object SendData: GatewayEvent()
    object ReceiveData: GatewayEvent()


}
```
**SendData** 
this event is used in the gateway Controller for sending the data to the broker.

**RecieveData** 
this event is used in the gateway controller for recieving data from broker


