package com.fogsim.fog

import com.fogsim.fog.cluster.Cluster
import com.fogsim.fog.core.AppCoroutineScope
import com.fogsim.fog.core.models.ApplicationState
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.EventType
import com.fogsim.fog.core.models.SensorType
import com.fogsim.fog.core.utils.EventMapper
import com.fogsim.fog.core.utils.ItemGenerator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.IOException
import java.net.InetSocketAddress


class MainRunner(port: Int) : WebSocketServer(InetSocketAddress(port)) {
    private val runDuration = 60L
    private var status = 0
    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }
    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
//        conn.send("Welcome to the server!") //This method sends a message to the new client
        broadcast(
            "new connection: " + handshake
                .resourceDescriptor
        ) //This method sends a message to all clients connected
        println(
            conn.remoteSocketAddress.address.hostAddress + " entered the room!"
        )
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        broadcast("$conn has left the room!")
        println("$conn has left the room!")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        var isData = false
        try {
            println(message)
            val typeToken = object : TypeToken<Command>() {}.type
            var command = Gson().fromJson<Command>(message, typeToken)
            if (command.type == CommandType.START) {
                broadcast("Started")
                eventListener()
                initApp(command)

            } else {
                broadcast("no")
            }
        }catch (e:Exception){
            print(e)
            print("Not Cluster Data")
        }



    }


    override fun onError(conn: WebSocket, ex: Exception) {
        ex.printStackTrace()
        // some errors like port binding failed may not be assignable to a specific websocket
    }

    override fun onStart() {
        println("Server started!")
        connectionLostTimeout = 0
        connectionLostTimeout = 100
    }

    private fun initApp(command: Command) {
        val clusterList: MutableList<Cluster>? = command.clusterList?.toMutableList()
        val generator = ItemGenerator(runDuration)
        val list = if (clusterList != null )  generator.addClusterList(clusterList) else generator.generateClusterList(1)

        println(list)
        val application = Application(
            "A1",
            name = "App",
            duration = command.maxSimulationTime.toLong(),
            list
        )
        Application.dataPlacementType = command.dataPlacementType
        Application.taskPlacementType = command.taskPlacementType
        Application.State = ApplicationState.STARTED
        application.start()
    }

    private fun eventListener() {
        try {
            EventEmitter.event.onEach {
                broadcast(Gson().toJson(it))
            }.launchIn(AppCoroutineScope())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    companion object {
        @Throws(InterruptedException::class, IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // if you want to run the app in terminal uncomment next line and comment others
            //initApp()

            // for running the app via websocket
            var port = 8887 // 843 flash policy port
            try {
                port = args[0].toInt()
            } catch (ex: Exception) {
            }
            val s = MainRunner(port)
            s.start()
            println("ChatServer started on port: " + s.port)
            // for running the app via websocket 
        }


    }
}

