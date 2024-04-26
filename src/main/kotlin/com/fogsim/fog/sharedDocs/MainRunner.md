# [Main Runner](https://github.com/MarsXan/fogSimulator/blob/version2/src/main/kotlin/com/fogsim/fog/MainRunner.kt)

```kotlin
class MainRunner(port: Int) : WebSocketServer(InetSocketAddress(port)) {
    private val runDuration = 60L
    private var status = 0
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
}
```
- This class is Runner of the whole simulation.
- It is responsible for creating and running all the components of the simulation. It is also responsible for the communication between the components. 
- It is a WebSocket server that listens for messages from the client. The client sends messages to the server to start the simulation, stop the simulation, and to get the status of the simulation. 
- We initialize the WebSocket server with the port number that we want to use in here.
- this class is start point of the simulation.

## Variables
- `runDuration` is the duration of the simulation in seconds.