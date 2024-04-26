package com.fogsim.fog.core.models

class DataConfig {
    var SD = 1.0

    // maximum data size
    var SMax = 300.0

    // variables are normalized between 0 and 1
    var a1 = 0.1
    var a2 = 0.3
    var a3 = 0.4
    var a4 = 0.5
    var a5 = 0.6

    var FAva = 2.0
    var FPar = 2.0

    var normalizedCount = 4.0


    override fun toString(): String {
        return "(a1=$a1, a2=$a2, a3=$a3, a4=$a4, a5=$a5)"
    }

    fun copy(): DataConfig {
        return DataConfig().apply {
            SD = SD
            SMax = SMax
            a1 = a1
            a2 = a2
            a3 = a3
            a4 = a4
            a5 = a5
            FAva = FAva
            FPar = FPar
            normalizedCount = normalizedCount}

    }


    fun toJson(): String {
        return """
            {
                "SD": $SD,
                "SMax": $SMax,
                "a1": $a1,
                "a2": $a2,
                "a3": $a3,
                "a4": $a4,
                "a5": $a5,
                "FAva": $FAva,
                "FPar": $FPar,
                "normalizedCount": $normalizedCount
            }
        """.trimIndent()
    }

}