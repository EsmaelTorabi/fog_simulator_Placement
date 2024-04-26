package com.fogsim.fog.sensor

import com.fogsim.fog.MainEvent

sealed class SensorEvent : MainEvent() {
    object SendData : SensorEvent()
}