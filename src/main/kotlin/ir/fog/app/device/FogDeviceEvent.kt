package ir.fog.app.device

import ir.fog.entities.CustomTuple

/**
 * @author mohsen on 1/13/23
 */
sealed  class FogDeviceEvent {
        object  Start : FogDeviceEvent()
        object  ShotDown : FogDeviceEvent()
        object  Stop : FogDeviceEvent()
        data   class  OnReciveTuple(val tuple: CustomTuple) : FogDeviceEvent()
}
