package com.fogsim.fog.core.models

data class Warning(
    var message: String? = null,
    var level: WarningLevel = WarningLevel.LOW,
    var time: Long? = null
){

    fun toJson(): String {
        return """
            {
                "message": "$message",
                "level": "${level.name}",
                "time": $time
            }
        """.trimIndent()
    }
}