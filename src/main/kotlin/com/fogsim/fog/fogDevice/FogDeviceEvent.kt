package com.fogsim.fog.fogDevice

import com.fogsim.fog.MainEvent
import com.fogsim.fog.core.models.AnalyzedData
import com.fogsim.fog.core.models.DataConfig
import com.fogsim.fog.core.models.SensorType
import com.fogsim.fog.core.models.Warning

sealed class FogDeviceEvent : MainEvent() {
    data class RequestMissingData(val type: SensorType) : FogDeviceEvent()
    data class SendWarning(val warning: Warning) : FogDeviceEvent()
    data class Analyze(val dataId: String) : FogDeviceEvent()
    data class HandleAnalyzedData(val analyzedData: AnalyzedData) : FogDeviceEvent()

    data class SendAnalyzedData(val analyzedData: AnalyzedData) : FogDeviceEvent()

    data class ProvideRequestedData(
        val type: SensorType,
        var targetDeviceId: String,
        var targetDeviceLocation: List<Double>
    ) : FogDeviceEvent()

    data class CatchRequestedData(val requestedData: AnalyzedData) : FogDeviceEvent()

    data class UpdateDataConfig(val dataId: String, val dataConfig: DataConfig) : FogDeviceEvent()
    data class UpdateDeviceConfig(val deviceConfig: List<Double>, val taskConfig: List<Double>) : FogDeviceEvent()


}
