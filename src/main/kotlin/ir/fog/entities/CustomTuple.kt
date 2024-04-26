package ir.fog.entities

import ir.fog.placement.PlacementConfigs
import org.cloudbus.cloudsim.UtilizationModel
import java.io.Serializable
import java.util.*


class CustomTuple(
    var ID: String,
    var appId: String?,
    var cloudletId: Int,
    var direction: Int,
    var cloudletLength: Long,
    var pesNumber: Int,
    var cloudletFileSize: Long,
    var cloudletOutputSize: Long,
    var utilizationModelCpu: UtilizationModel?,
    var utilizationModelRam: UtilizationModel?,
    var utilizationModelBw: UtilizationModel?,
    // 2x of transmission time
    var expireTime: Double = 0.02,
    var type: TupleType,
    var isProcessible: Boolean?,
    var configs: PlacementConfigs,
    var creationDate: Date = Date(),
    var userId: Int? = null,
    var tupleType: String? = null,
    var isCloudTuple:Boolean = false

) : Serializable {
    var runtimeID: String = ""
    var enterDeviceTime: Date = Date()
}
