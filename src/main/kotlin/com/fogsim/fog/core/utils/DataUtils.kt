package com.fogsim.fog.core.utils

import kotlin.math.pow

object DataUtils {
     fun dataSizeInBytes(data: String): Int = data.length * Char.SIZE_BITS

     fun binaryToDecimal(binary: String): Int{
          val reversedDigits = binary.reversed().toCharArray().map{it.digitToInt()}
          var decimalNumber = 0

          for ((i, n) in reversedDigits.withIndex()) {
               decimalNumber += (n * 2.0.pow(i)).toInt()
          }
          return decimalNumber
     }
}