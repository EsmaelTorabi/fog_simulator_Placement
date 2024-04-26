package com.fogsim.fog.server

import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.models.*
import com.fogsim.fog.fogDevice.FogDevice

sealed class ServerEvent : MainEvent() {
    data class RequestMissingData(
        val type: SensorType,
        val deviceId: String,
        val deviceLocation: List<Double>
    ) : ServerEvent()

    data class TransferToDevice(val deviceId: String, val requestedData: Data) : ServerEvent()
    data class UpdateDevice(val device: FogDevice) : ServerEvent()
    data class HandleWarning(val deviceId: String, val warning: Warning) : ServerEvent()
    data class SendTaskToDevice(val deviceId: String) : ServerEvent()
    data class HandleAnalyzedData(val analyzedData: AnalyzedData, var analyzerDevice: FogDevice) : ServerEvent()
    object CheckAllTasksFinished : ServerEvent()
    data class UpdateDataConfig(val dataId: String, val dataConfig: DataConfig) : ServerEvent()

    data class IncreaseNumberOfCopies(val targetData: Data,val count: Int) : ServerEvent()
    data class DecreaseNumberOfCopies(val targetData: Data,val count: Int) : ServerEvent()
}
