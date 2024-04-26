package com.fogsim.fog.broker

import com.fogsim.fog.MainEvent

sealed class BrokerEvent:MainEvent(){
    object SendData: BrokerEvent()
    object ReceiveData: BrokerEvent()
    object SendDataToCloud: BrokerEvent()

}
