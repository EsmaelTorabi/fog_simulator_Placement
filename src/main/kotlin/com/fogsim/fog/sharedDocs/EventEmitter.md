# [Event Emitter](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/EventEmitter.kt)

![EventEmitter](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/statics/eventEmitter/eventEmitter.png)

event emitter is a class that can be used to emit events and listen to them.in this project we use it to emit events from entities.
when an entity emits an event, all the listeners of that event will be notified.we use this mechanism to notify the fog nodes about the events that are happening in the system.it means that when an entity emits an event, all the fog nodes will be notified about that event and they can react to it if the event is related to them.
for example, when an entity receives a task, it emits an event to notify the other entities about the task that it has received and they can react to it if the task is related to them.

## class structure
```kotlin
object EventEmitter {

}
```
we use a singleton object to implement the event emitter class.it means that we can access the event emitter from anywhere in the project.

- for example, we can use the event emitter in the following way:
```kotlin
EventEmitter.emmit(MainEvent()) {
}
```

in the above code, we emit an event of type MainEvent and we can listen to that event in the block that is passed to the emit function.

## variables
```kotlin
 private val baseEvent = MutableSharedFlow<MainEvent>()
```
we use a MutableSharedFlow to store the events that are emitted by the entities.

```kotlin
private val scope = AppCoroutineScope()
```
we use a coroutine scope to launch a coroutine to listen to the events that are emitted by the entities.

```kotlin
private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }
```
we use a coroutine exception handler to handle the exceptions that are thrown in the coroutine that is launched to listen to the events.

```kotlin
var event = baseEvent.asSharedFlow()
```
we use a SharedFlow to expose the events to the other classes. in other entities, we can listen to the events with this variable in the following way:
```kotlin
EventEmitter.event.collect {
}
```

## functions
```kotlin
   fun emit(event: MainEvent) {
        scope.launch(coroutineExceptionHandler) {
            baseEvent.emit(event)
        }
    }
```
we use this function to emit an event. we launch a coroutine to emit the event in the coroutine scope.
all the entities that are listening to the events will be notified about the event. and if the event is related to them, they can react to it.
or if any entity wants to send an event to the other entities, it can use this function to emit the event.

```kotlin
fun emit(event: MainEvent, delay: Long) {
        scope.launch(coroutineExceptionHandler) {
            kotlinx.coroutines.delay(delay)
            baseEvent.emit(event)
        }
    }
```

we use this function to emit an event after a delay. we launch a coroutine to emit the event in the coroutine scope.
all the entities that are listening to the events will be notified about the event. and if the event is related to them, they can react to it.
or if any entity wants to send an event to the other entities, it can use this function to emit the event after a delay.


