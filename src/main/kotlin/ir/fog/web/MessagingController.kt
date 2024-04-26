package ir.fog.web

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.fog.core.EventBus
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.core.Utils
import ir.fog.entities.Cluster
import ir.fog.app.device.Device
import ir.fog.placement.CustomController
import org.json.simple.JSONArray
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.util.*


/**
 * @author mohsen on 12/21/21
 */
@Controller
class MessagingController {
    var controller: CustomController? = null


    @Autowired
    private var template: SimpMessagingTemplate? = null


    @MessageMapping("/start")
    fun start(data: JSONArray) {
        EventBus.restart()
        val typeToken = object : TypeToken<List<Cluster>>() {}.type

        val clusterList = Gson().fromJson<List<Cluster>>(data.toString(), typeToken)


        clusterList.forEachIndexed { index, cluster ->
            cluster.id = "${cluster.name}-$index" // cluster-0
            cluster.server!!.id = "${cluster.server!!.name}-$index"
            cluster.broker?.id = "${cluster.broker?.name}-$index"
            cluster.broker?.serverId = "${cluster.server!!.name}-$index"
            cluster.gateway?.id = "${cluster.gateway?.name}-$index"
            cluster.gateway?.brokerId = "${cluster.broker?.name}-$index"
            cluster.fogDeviceList?.forEachIndexed { i, device ->
                device.id = "${device.name}-$index-$i"
                device.createdTime = Date()
                device.host = Utils.generateHost(
                    storage = device.totalSpace.toLong(),
                    busyPower = device.busyPower,
                    idlePower = device.idlePower,
                    mips = device.ratePerMips
                )
                device.queue = mutableListOf()
                device.serverId = "${cluster.server!!.name}-$index"
            }
            cluster.sensorList?.forEachIndexed { i, sensor ->
                sensor.id = "${sensor.name}-$index-$i"
                sensor.gatewayId = "${cluster.gateway?.name}-$index"
                sensor.tupleCpuLength = 3000
                sensor.tupleNwLength = 2000
            }
        }

        val deviceList = mutableListOf<Device>()
        clusterList.first().server!!.initializeVariables()
        deviceList.add(clusterList.first().server!!)
        deviceList.add(clusterList.first().gateway!!)
        deviceList.addAll(clusterList.first().fogDeviceList!!)
        controller = CustomController(clusterList.first().broker!!, deviceList, clusterList.first().sensorList!!)
        controller?.maxSimulationTime = (clusterList.first().maxSimulationTime).toLong()
        val event =
            FogEvent(entityId = "controller", delay = 100.0, eventType = SimEvents.INITIALIZE_APP, data = clusterList);
        this.template!!.convertAndSend("/fog/events", event)


        controller?.start()

        EventBus.observe()
            .subscribe({
                if (it.eventType == SimEvents.START_SIMULATION)
                    println("@@@@@@@@@@@@$it")
                if (this.template != null)
                    this.template!!.convertAndSend("/fog/events", it)
            }) {}

        Thread.currentThread().join()
    }


}


// ram to device
// app edge sensor
// selectivity
