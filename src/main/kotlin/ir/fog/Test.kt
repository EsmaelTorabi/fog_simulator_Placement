package ir.fog

import ir.fog.app.broker.CustomBroker
import ir.fog.app.device.CustomFogDevice
import ir.fog.app.device.Device
import ir.fog.app.device.DeviceType
import ir.fog.app.gateway.Gateway
import ir.fog.app.sensor.CustomSensor
import ir.fog.core.Utils
import ir.fog.entities.*
import ir.fog.app.server.DataPlacementType
import ir.fog.app.server.Server
import ir.fog.app.server.TaskPlacementType
import ir.fog.placement.CustomController
import ir.fog.placement.PlacementConfigs
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.FogLinearPowerModel
import org.fog.utils.FogUtils
import java.util.*


object Test {
    var sensorList: MutableList<CustomSensor> = mutableListOf()
    var deviceList: MutableList<Device> = mutableListOf()
    var sensorNames = Utils.generateSensorList(13)
    var cpuValueList = Utils.generateCpuLengthList(13)
    var nwValueList = Utils.generateNetworkLengthList(13)
    private var numberOfFogDevices = 20


    @JvmStatic
    fun main(args: Array<String>) {

        val appId = "vr_game"




        initSensors(appId, 0)
        initGateWay()
        initServer()
        initDevice()

        val controller = CustomController(createBroker(), deviceList, sensorList)

        controller.start()
        Thread.currentThread().join()
    }

    fun initGateWay() {
        createFogDevice(
            DeviceType.GATEWAY, "gateway", 1000, 1000,
            10000, 270, 3, 0.0, 87.53,
            82.44
        )

    }

    fun initServer() {
        createFogDevice(
            DeviceType.SERVER, "server", 1000, 1000,
            10000, 270, 3, 0.0, 87.53,
            82.44
        )

    }

    fun initDevice() {
        for (i in 0 until numberOfFogDevices) {
            createFogDevice(
                DeviceType.FOG_DEVICE, "device-$i",
                7000,
                7000,
                15000,
                300, 3, 0.0,
                90.0,
                40.0,
                index = i
            )
        }


    }

    fun initSensors(appId: String, userId: Int) {
        createSensors(
            userId,
            appId,
            sensorNames.map { it.uppercase() }, sensorNames
        )
    }

    fun createFogDevice(
        type: DeviceType,
        nodeName: String, mips: Long,
        ram: Int, upBw: Long, downBw: Long, level: Int,
        ratePerMips: Double, busyPower: Double, idlePower: Double,
        index: Int = 0
    ) {
        val peList: MutableList<Pe> = ArrayList()

        // 3. Create PEs and add these into a list.
        peList.add(Pe(0, PeProvisionerOverbooking(mips.toDouble()))) // need to store Pe id and MIPS Rating
        val hostId = FogUtils.generateEntityId()
        val storage: Long = getStorage(type) // host storage
        val bw = 10000
        val host = PowerHost(
            hostId,
            RamProvisionerSimple(ram),
            BwProvisionerOverbooking(bw.toLong()),
            storage,
            peList,
            StreamOperatorScheduler(peList),
            FogLinearPowerModel(busyPower, idlePower)
        )
        val hostList: MutableList<Host> = ArrayList()
        hostList.add(host)
        val arch = "x86" // system architecture
        val os = "Linux" // operating system
        val vmm = "Xen"
        val time_zone = 10.0 // time zone this resource located
        val cost = 3.0 // the cost of using processing in this resource
        val costPerMem = 0.05 // the cost of using memory in this resource
        val costPerStorage = 0.001 // the cost of using storage in this
        // resource
        val costPerBw = 0.0 // the cost of using bw in this resource
        val storageList = LinkedList<Storage>() // we are not adding SAN
        // devices by now
        val characteristics = CustomFogDeviceCharacteristics(
            arch,
            os,
            vmm,
            host,
            time_zone,
            cost,
            costPerMem,
            costPerStorage,
            costPerBw,
            120,
            0.4,
            0.5,
            0.6,
            0.7
        )

        when (type) {
            DeviceType.FOG_DEVICE -> {
                val device = CustomFogDevice(
                    id = nodeName,
                    nodeName,
                    characteristics,
                    AppModuleAllocationPolicy(hostList),
                    storageList,
                    10.0,
                    upBw.toDouble(),
                    downBw.toDouble(),
                    0.0,
                    ratePerMips,
                    busyPower = 90.0,
                    idlePower = 40.0,
                    host = host,
                )

                device.emptySpace = device.host.storage.toDouble()
                device.CCk = index.toDouble()
                device.serverToDeviceBw = 10.0 * index
                device.characteristics.flu = 0.5
                device.distance = index / 10.0

                deviceList.add(
                    device
                )
            }

            DeviceType.SERVER -> {

                val device = Server(
                    id = nodeName,
                    nodeName,
                    characteristics,
                    AppModuleAllocationPolicy(hostList),
                    storageList,
                    10.0,
                    upBw.toDouble(),
                    downBw.toDouble(),
                    0.0,
                    ratePerMips,
                    dataPlacementType = DataPlacementType.CUSTOM_CA_REPLICA,
                    taskPlacementType = TaskPlacementType.CUSTOM_PERFORMANCE_AWARE
                )
                deviceList.add(
                    device
                )
            }

            DeviceType.GATEWAY -> {
                val device = Gateway(
                    id = nodeName,
                    nodeName,
                    characteristics,
                    AppModuleAllocationPolicy(hostList),
                    storageList,
                    10.0,
                    upBw.toDouble(),
                    downBw.toDouble(),
                    0.0,
                    ratePerMips
                )
                deviceList.add(device)
            }
        }


    }

    fun getStorage(deviceType: DeviceType): Long {
        return when (deviceType) {
            DeviceType.FOG_DEVICE -> 50000
            DeviceType.SERVER -> 8000000
            DeviceType.GATEWAY -> 1000000
        }
    }


    fun createBroker(): CustomBroker {
        return CustomBroker(
            "0", "broker", 100, 20000, 200,
            10000, 400000, 400000, isActive = true
        )
    }

    private fun createSensors(
        userId: Int,
        appId: String,
        sensors: List<String>,
        types: List<String>
    ) {
        val configs = PlacementConfigs()
        configs.a1 = 0.3
        configs.a2 = 0.4
        configs.a3 = 0.5
        configs.a4 = 0.6
        configs.a5 = 0.5

        for (i in sensors.indices) {
            val sensor = CustomSensor(
                "S-$i",
                sensors[i],
                types[i],
                userId,
                appId,
                8.0,
                config = configs,
                tupleCpuLength = cpuValueList[i].toLong(),
                tupleNwLength = nwValueList[i].toLong()
            )

            sensorList.add(sensor)
        }

    }


}
