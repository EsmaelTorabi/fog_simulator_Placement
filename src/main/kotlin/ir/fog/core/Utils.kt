package ir.fog.core

import ir.fog.entities.CustomFogDeviceCharacteristics
import ir.fog.app.gateway.Gateway
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.application.AppEdge
import org.fog.application.Application
import org.fog.entities.Tuple
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.FogLinearPowerModel
import org.fog.utils.FogUtils
import java.util.*

/**
 * @author mohsen on 12/24/21
 */
object Utils {
    fun createApplication(appId: String, userId: Int, types: List<String>): Application? {
        val application =
            Application.createApplication(appId, userId) // creates an empty application model (empty directed graph)

        application.addAppEdge("HEAT", "client", 3000.0, 9000.0, "HEAT", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("LIGHT", "client", 3000.0, 500.0, "LIGHT", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("SOUND", "client", 3000.0, 500.0, "SOUND", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("SMOKE", "client", 3000.0, 500.0, "SMOKE", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("XX", "client", 3000.0, 500.0, "XX", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("YY", "client", 3000.0, 500.0, "YY", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("ZZ", "client", 6000.0, 500.0, "ZZ", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("RR", "client", 5000.0, 1500.0, "RR", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("FF", "client", 6000.0, 1000.0, "FF", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("OO", "client", 4000.0, 2000.0, "OO", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("QQ", "client", 3000.0, 2000.0, "QQ", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("EE", "client", 1000.0, 2000.0, "EE", Tuple.UP, AppEdge.SENSOR)
        application.addAppEdge("CC", "client", 1000.0, 2000.0, "CC", Tuple.UP, AppEdge.SENSOR)

        return application
    }

    fun generateGateway(
        upBw: Double = 10000.0,
        downBw: Double = 270.0,
        ratePerMips: Double = 0.0,
        gateway: Gateway,
        characteristics: CustomFogDeviceCharacteristics = generateCharacteristics(
            generateHost(
                ram = 1000,
                mips = 1000.0,
                storage = 1000000,
                busyPower = 87.53,
                idlePower = 82.44
            )
        ),
        host: Host = generateHost(
            ram = 1000,
            mips = 1000.0,
            storage = 1000000,
            busyPower = 87.53,
            idlePower = 82.44
        )
    ): Gateway {
        val storageList = LinkedList<Storage>()
        gateway.uplinkBandwidth = upBw
        gateway.downlinkBandwidth = downBw
        gateway.ratePerMips = ratePerMips
        gateway.characteristics = characteristics
        gateway.vmAllocationPolicy = AppModuleAllocationPolicy(
            listOf(host)
        )
        gateway.storageList = storageList

        return gateway
    }

    fun generateBroker() {

    }

//    fun generateServer(
//        upBw: Double = 10000.0,
//        downBw: Double = 270.0,
//        ratePerMips: Double = 0.0,
//        server: Server,
//        characteristics: CustomFogDeviceCharacteristics = generateCharacteristics(
//            generateHost(
//                ram = 1000,
//                mips = 1000.0,
//                storage = 8000000,
//                busyPower = 87.53,
//                idlePower = 82.44
//            )
//        ),
//        host: Host = generateHost(
//            ram = 1000,
//            mips = 1000.0,
//            storage = 8000000,
//            busyPower = 87.53,
//            idlePower = 82.44
//        )
//    ): Server {
//        server.uplinkBandwidth = upBw
//        server.downlinkBandwidth = downBw
//        server.ratePerMips = ratePerMips
//        server.characteristics = characteristics
//        server.storageList = LinkedList<Storage>()
//
//        return server
//    }

    fun generateCharacteristics(
        host: Host = generateHost(
            ram = 1000,
            mips = 1000.0,
            storage = 1000000,
            busyPower = 87.53,
            idlePower = 82.44
        )
    ): CustomFogDeviceCharacteristics {
        val arch = "x86" // system architecture
        val os = "Linux" // operating system
        val vmm = "Xen"
        val time_zone = 10.0 // time zone this resource located
        val cost = 3.0 // the cost of using processing in this resource
        val costPerMem = 0.05 // the cost of using memory in this resource
        val costPerStorage = 0.001 // the cost of using storage in this
        // resource
        val costPerBw = 0.0 // the cost of using bw in this resource
        // devices by now
        return CustomFogDeviceCharacteristics(
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
    }

    fun generateHost(
        ram: Int = 1000,
        storage: Long,
        busyPower: Double,
        idlePower: Double,
        mips: Double
    ): Host {
        val peList: MutableList<Pe> = ArrayList()
        peList.add(Pe(0, PeProvisionerOverbooking(mips)))
        val hostId = FogUtils.generateEntityId() // host storage
        val bw = 10000

        return PowerHost(
            hostId,
            RamProvisionerSimple(ram),
            BwProvisionerOverbooking(bw.toLong()),
            storage,
            peList,
            StreamOperatorScheduler(peList),
            FogLinearPowerModel(busyPower, idlePower)
        )
    }


    fun generateSensorList(count: Int): MutableList<String> {
        val sList = mutableListOf<String>()
        for (i in 0..count) {
            sList.add("sensor-$i")
        }
        return sList
    }

    fun generateCpuLengthList(count: Int): MutableList<Double> {
        val cList = mutableListOf<Double>()
        var initValue = 3000.0
        for (i in 0..count) {
            if (i % 5 == 0)
                initValue += 2000
            cList.add(initValue)
        }
        return cList
    }

    fun generateNetworkLengthList(count: Int): MutableList<Double> {
        val nList = mutableListOf<Double>()
        var initValue = 500.0
        for (i in 0..count) {
            if (i % 3 == 0)
                initValue += 1000
            nList.add(initValue)
        }
        return nList
    }


}
