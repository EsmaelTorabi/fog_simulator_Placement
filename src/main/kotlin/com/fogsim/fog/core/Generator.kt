package com.fogsim.fog.core

object Generator {
    fun clusterID(index:Int):String  = "C$index"
    fun gatewayID(index:Int):String  = "C${index}G"
    fun brokerID(index:Int):String  = "C${index}B"
    fun serverID(index:Int):String  = "C${index}S"
    fun deviceID(clusterName:String, deviceIndex:Int):String  = "${clusterName}D${deviceIndex}"
    fun sensorID(clusterName:String, sensorIndex:Int):String  = "${clusterName}Sensor${sensorIndex}"

}