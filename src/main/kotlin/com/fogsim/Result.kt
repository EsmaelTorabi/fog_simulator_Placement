package com.fogsim

class AppResult (
    var uploadCost:Double = 0.0,
    var downloadCost:Double = 0.0,
    var energyCost:Double = 0.0,
    var transferCost:Double = 0.0,
    var runtime:Double = 0.0,
    var storageCost:Double = 0.0,
    var accessibility:Double = 0.0,
    var availability:Double = 0.0,
    var totalCpuTime:Double = 0.0,
    var networkCost:Double = 0.0
){
    fun toJson(): String {
        return """
            {
                "uploadCost": $uploadCost,
                "downloadCost": $downloadCost,
                "energyCost": $energyCost,
                "transferCost": $transferCost,
                "runtime": $runtime,
                "storageCost": $storageCost,
                "accessibility": $accessibility,
                "availability": $availability,
                "totalCpuTime": $totalCpuTime,
                "networkCost": $networkCost
            }
        """.trimIndent()
    }
}