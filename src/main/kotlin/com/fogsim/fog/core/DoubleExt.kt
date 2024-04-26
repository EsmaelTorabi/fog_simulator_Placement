package com.fogsim.fog.core

fun Double.roundTo(n : Int) : Double {
    return "%.${n}f".format(this).toDouble()
}