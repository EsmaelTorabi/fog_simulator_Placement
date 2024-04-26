package com.fogsim.fog.gateway

import com.fogsim.fog.MainEvent

sealed class GatewayEvent : MainEvent() {
    object SendData: GatewayEvent()
    object ReceiveData: GatewayEvent()


}