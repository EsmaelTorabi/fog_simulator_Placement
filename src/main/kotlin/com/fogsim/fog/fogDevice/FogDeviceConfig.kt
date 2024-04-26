package com.fogsim.fog.fogDevice

class FogDeviceConfig(
    var db1: Double = 0.1,
    var db2: Double = 0.1,
    var db3: Double = 0.1,
    var db4: Double = 0.1,
    var db5: Double = 0.1,
    var currentAttendanceTime: Double = 0.0,
    var devicePresenceMiddle: Double = 0.0,
    var devicesPresenceVariance: Double = 0.0,
    var totalMips: Double = 0.0,
    var uploadBW: Double = 100.0,
    var downloadBW: Double = 100.0,
   var architecture: String?, os: String?, vmm: String?, var costPerMips: Double, var costPerMem: Double,
    var costPerStorage: Double, val costPerBusyTime: Double = 100.0, val costPerIdleTime: Double = 20.0,
    var RPI: Int = 2, var flu: Double, var tb1: Double, var tb2: Double, var tb3: Double, var minDataSize: Int = 200,
    var minStoragePercent: Double = 0.6,
    var minFlue: Double = 0.5,
    var minQueue: Int = 2,
    var minExpireTime: Int = 1,
    var aEpsilon: Double = 0.4,
    var bEpsilon: Double = 0.4,
    var tEpsilon: Double = 0.4,


){
    fun toJson(): String {
        return """
            {
                "db1": $db1,
                "db2": $db2,
                "db3": $db3,
                "db4": $db4,
                "db5": $db5,
                "currentAttendanceTime": $currentAttendanceTime,
                "devicePresenceMiddle": $devicePresenceMiddle,
                "devicesPresenceVariance": $devicesPresenceVariance,
                "totalMips": $totalMips,
                "uploadBW": $uploadBW,
                "downloadBW": $downloadBW,
                "costPerMips": $costPerMips,
                "costPerMem": $costPerMem,
                "costPerStorage": $costPerStorage,
                "costPerBusyTime": $costPerBusyTime,
                "costPerIdleTime": $costPerIdleTime,
                "RPI": $RPI,
                "flu": $flu,
                "tb1": $tb1,
                "tb2": $tb2,
                "tb3": $tb3,
                "minDataSize": $minDataSize,
                "minStoragePercent": $minStoragePercent,
                "minFlue": $minFlue,
                "minQueue": $minQueue,
                "minExpireTime": $minExpireTime,
                "aEpsilon": $aEpsilon,
                "bEpsilon": $bEpsilon,
                "tEpsilon": $tEpsilon
            }
        """.trimIndent()
    }
}