package com.fogsim.fog.core.utils

import kotlin.math.pow
import kotlin.math.sqrt

object LocationUtils {
     fun getDistance(sLocation: List<Double>, dLocation: List<Double>): Double {
        return sqrt(
            (sLocation.first() - dLocation.first()).pow(2.0) + (sLocation[1] - dLocation[1]).pow(2.0)
        )
    }

}
