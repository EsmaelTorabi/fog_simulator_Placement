package ir.fog.placement

import java.io.Serializable

/**
 * @author mohsen on 9/27/21
 */

class PlacementConfigs:Serializable {
    // Absolute data size in MB
    var SD = 1.0

    // maximum data size
    var SMax = 10.0

    // variables are normalized between 0 and 1
    var a1 = 0.1
    var a2 = 0.2
    var a3 = 0.3
    var a4 = 0.4
    var a5 = 0.5

    var FAva = 2.0
    var FPar = 2.0

    var normalizedCount = 4.0

    // variables are normalized between 0 and 1
    var b1 = 0.0
    var b2 = 0.0
    var b3 = 0.0
    var b4 = 0.0
    var b5 = 0.0


}
