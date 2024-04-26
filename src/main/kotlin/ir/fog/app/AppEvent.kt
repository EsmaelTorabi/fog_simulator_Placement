package ir.fog.app

import ir.fog.core.FogEvent

/**
 * @author mohsen on 1/13/23
 */
sealed class AppEvent {
    data class InitializeApp(val fogEvent: FogEvent) : AppEvent()
    data class StartSimulation(val fogEvent: FogEvent) : AppEvent()
    data class GatewayTuple(val fogEvent: FogEvent) : AppEvent()
    data class BrokerTuple(val fogEvent: FogEvent) : AppEvent()
    data class ServerTuple(val fogEvent: FogEvent) : AppEvent()
    data class DeviceCopyTuple(val fogEvent: FogEvent) : AppEvent()
    data class DeviceProcessTuple(val fogEvent: FogEvent) : AppEvent()
    data class UpdateTuple(val fogEvent: FogEvent) : AppEvent()
    data class ActuatorTuple(val fogEvent: FogEvent) : AppEvent()
    data class DeviceDeleteTuple(val fogEvent: FogEvent) : AppEvent()
    data class MonitorDeleteTuple(val fogEvent: FogEvent) : AppEvent()
    data class UpdateDeviceTuple(val fogEvent: FogEvent) : AppEvent()
    data class TupleProcessFinished(val fogEvent: FogEvent) : AppEvent()
    data class DecreaseNumberOfTuple(val fogEvent: FogEvent) : AppEvent()
    data class IncreaseNumberOfTuple(val fogEvent: FogEvent) : AppEvent()
    data class UpdateTupleConfig(val fogEvent: FogEvent) : AppEvent()
    data class UpdateDeviceConfig(val fogEvent: FogEvent) : AppEvent()
    data class UpdateDeviceTaskConfig(val fogEvent: FogEvent) : AppEvent()
    data class DeleteDeviceTuple(val fogEvent: FogEvent) : AppEvent()
    data class ShotDownFogDevice(val fogEvent: FogEvent) : AppEvent()
    data class AddFogDevice(val fogEvent: FogEvent) : AppEvent()
    data class StopExecution(val fogEvent: FogEvent) : AppEvent()
    data class UpdateNetworkUsage(val fogEvent: FogEvent) : AppEvent()
    data class UpdateNetworkUsageWeb(val fogEvent: FogEvent) : AppEvent()
    data class UpdateEnergyConsumed(val fogEvent: FogEvent) : AppEvent()
    data class UpdateEnergyConsumedWeb(val fogEvent: FogEvent) : AppEvent()
    data class UpdateTupleRunTime(val fogEvent: FogEvent) : AppEvent()
    data class UpdateTupleRunTimeWeb(val fogEvent: FogEvent) : AppEvent()
    data class UpdateDeviceCost(val fogEvent: FogEvent) : AppEvent()
    data class UpdateDeviceCostWeb(val fogEvent: FogEvent) : AppEvent()
    data class CheckForFinishTask(val fogEvent: FogEvent) : AppEvent()
    data class QueueFinished(val fogEvent: FogEvent) : AppEvent()
    data class StopSystem(val fogEvent: FogEvent) : AppEvent()
    data class CloudTuple(val fogEvent: FogEvent) : AppEvent()
    data class ShowResults(val fogEvent: FogEvent) : AppEvent()

}


