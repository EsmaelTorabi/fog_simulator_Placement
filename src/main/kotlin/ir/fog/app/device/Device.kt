package ir.fog.app.device

import java.io.Serializable

/**
 * @author mohsen on 3/2/22
 */
abstract class Device(
    var id: String,
    var name: String
): Serializable {
   abstract fun startEntity()
}
