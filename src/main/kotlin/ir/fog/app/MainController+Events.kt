package ir.fog.app

/**
 * @author mohsen on 1/13/23
 */

fun MainController.handleEvents(appEvent: AppEvent) {
    when (appEvent) {
        is AppEvent.InitializeApp -> initializeApp(appEvent.fogEvent)
        is AppEvent.StartSimulation -> startApp(appEvent.fogEvent)
        is AppEvent.GatewayTuple -> gatewayTuple(appEvent.fogEvent)
        is AppEvent.BrokerTuple -> brokerTuple(appEvent.fogEvent)
        is AppEvent.ServerTuple -> serverTuple(appEvent.fogEvent)
        is AppEvent.DeviceCopyTuple -> deviceCopyTuple(appEvent.fogEvent)
        is AppEvent.DeviceProcessTuple -> deviceProcessTuple(appEvent.fogEvent)
        is AppEvent.UpdateTuple -> updateTuple(appEvent.fogEvent)
        is AppEvent.ActuatorTuple -> actuatorTuple(appEvent.fogEvent)
        is AppEvent.DeviceDeleteTuple -> deviceDeleteTuple(appEvent.fogEvent)
        is AppEvent.MonitorDeleteTuple -> monitorDeleteTuple(appEvent.fogEvent)
        is AppEvent.UpdateDeviceTuple -> updateDeviceTuple(appEvent.fogEvent)
        is AppEvent.TupleProcessFinished -> tupleProcessFinished(appEvent.fogEvent)
        is AppEvent.DecreaseNumberOfTuple -> decreaseNumberOfTuple(appEvent.fogEvent)
        is AppEvent.IncreaseNumberOfTuple -> increaseNumberOfTuple(appEvent.fogEvent)
        is AppEvent.UpdateTupleConfig -> updateTupleConfig(appEvent.fogEvent)
        is AppEvent.UpdateDeviceConfig -> updateDeviceConfig(appEvent.fogEvent)
        is AppEvent.UpdateDeviceTaskConfig -> updateDeviceTaskConfig(appEvent.fogEvent)
        is AppEvent.DeleteDeviceTuple -> deleteDeviceTuple(appEvent.fogEvent)
        is AppEvent.ShotDownFogDevice -> shotDownFogDevice(appEvent.fogEvent)
        is AppEvent.AddFogDevice -> addFogDevice(appEvent.fogEvent)
        is AppEvent.CheckForFinishTask -> checkForFinishTask(appEvent.fogEvent)
        is AppEvent.CloudTuple -> cloudTuple(appEvent.fogEvent)
        is AppEvent.QueueFinished -> queueFinished(appEvent.fogEvent)
        is AppEvent.ShowResults -> showResults(appEvent.fogEvent)
        is AppEvent.StopExecution -> stopExecution(appEvent.fogEvent)
        is AppEvent.StopSystem -> stopSystem(appEvent.fogEvent)
        is AppEvent.UpdateDeviceCost -> updateDeviceCost(appEvent.fogEvent)
        is AppEvent.UpdateDeviceCostWeb -> updateDeviceCostWeb(appEvent.fogEvent)
        is AppEvent.UpdateEnergyConsumed -> updateEnergyConsumed(appEvent.fogEvent)
        is AppEvent.UpdateEnergyConsumedWeb -> updateEnergyConsumedWeb(appEvent.fogEvent)
        is AppEvent.UpdateNetworkUsage -> updateNetworkUsage(appEvent.fogEvent)
        is AppEvent.UpdateNetworkUsageWeb -> updateNetworkUsageWeb(appEvent.fogEvent)
        is AppEvent.UpdateTupleRunTime -> updateTupleRunTime(appEvent.fogEvent)
        is AppEvent.UpdateTupleRunTimeWeb -> updateTupleRunTimeWeb(appEvent.fogEvent)
    }
}


