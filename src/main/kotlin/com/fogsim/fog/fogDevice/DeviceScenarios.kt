package com.fogsim.fog.fogDevice

import com.fogsim.fog.core.models.AnalyzedData
import com.fogsim.fog.core.models.SensorType
import com.fogsim.fog.core.models.Warning
import com.fogsim.fog.core.models.WarningLevel

object DeviceScenarios {
    fun fireWarning(dataList: List<AnalyzedData>): Warning {
        val fire = Fire()
        for (data in dataList) {
            if (data.sensorType == SensorType.TEMPERATURE) {
                fire.temp = data.value
            }
            if (data.sensorType == SensorType.SMOKE) {
                fire.smoke = data.value
            }
            if (data.sensorType == SensorType.GAS) {
                fire.gas = data.value
            }
        }
        if (fire.temp > 40.0 && fire.smoke > 20.0 && fire.gas > 20.0) {
            return Warning("Fire detected", WarningLevel.HIGH, System.currentTimeMillis())
        } else if (fire.temp > 30.0 && fire.smoke > 10.0 && fire.gas > 10.0) {
            return Warning("Fire detected", WarningLevel.MEDIUM, System.currentTimeMillis())
        } else if (fire.temp > 20.0 && fire.smoke > 5.0 && fire.gas > 5.0) {
            return Warning("Fire detected", WarningLevel.LOW, System.currentTimeMillis())
        }
        return Warning("No fire detected", WarningLevel.NONE, System.currentTimeMillis())
    }

    fun earthquakeWarning(dataList: List<AnalyzedData>): Warning {
        val earthquake = Earthquake()
        for (data in dataList) {
            if (data.sensorType == SensorType.ACCELEROMETER) {
                earthquake.accelerometer = data.value
            }
            if (data.sensorType == SensorType.GYROSCOPE) {
                earthquake.gyroscope = data.value
            }
            if (data.sensorType == SensorType.MAGNETOMETER) {
                earthquake.magnetometer = data.value
            }
        }
        if (earthquake.accelerometer > 40.0 && earthquake.gyroscope > 40.0 && earthquake.magnetometer > 40.0) {
            return Warning("Earthquake detected", WarningLevel.HIGH, System.currentTimeMillis())
        } else if (earthquake.accelerometer > 30.0 && earthquake.gyroscope > 30.0 && earthquake.magnetometer > 30.0) {
            return Warning("Earthquake detected", WarningLevel.MEDIUM, System.currentTimeMillis())
        } else if (earthquake.accelerometer > 20.0 && earthquake.gyroscope > 20.0 && earthquake.magnetometer > 20.0) {
            return Warning("Earthquake detected", WarningLevel.LOW, System.currentTimeMillis())
        }
        return Warning("No earthquake detected", WarningLevel.NONE, System.currentTimeMillis())
    }

    fun stormWarning(dataList: List<AnalyzedData>): Warning {
        val storm = Storm()
        for (data in dataList) {
            if (data.sensorType == SensorType.PRESSURE) {
                storm.pressure = data.value
            }
            if (data.sensorType == SensorType.TEMPERATURE) {
                storm.temp = data.value
            }
            if (data.sensorType == SensorType.HUMIDITY) {
                storm.humidity = data.value
            }
        }
        if (storm.pressure > 40.0 && storm.temp > 40.0 && storm.humidity > 40.0) {
            return Warning("Storm detected", WarningLevel.HIGH, System.currentTimeMillis())
        } else if (storm.pressure > 30.0 && storm.temp > 30.0 && storm.humidity > 30.0) {
            return Warning("Storm detected", WarningLevel.MEDIUM, System.currentTimeMillis())
        } else if (storm.pressure > 20.0 && storm.temp > 20.0 && storm.humidity > 20.0) {
            return Warning("Storm detected", WarningLevel.LOW, System.currentTimeMillis())
        }
        return Warning("No storm detected", WarningLevel.NONE, System.currentTimeMillis())
    }
}

class Fire {
    var sensors = mutableListOf(SensorType.SMOKE, SensorType.GAS, SensorType.TEMPERATURE)
    var temp = 0.0
    var smoke = 0.0
    var gas = 0.0
}

class Earthquake {
    var sensors = mutableListOf(SensorType.ACCELEROMETER, SensorType.GYROSCOPE, SensorType.MAGNETOMETER)
    var accelerometer = 0.0
    var gyroscope = 0.0
    var magnetometer = 0.0
}

class Storm {
    var sensors = mutableListOf(SensorType.PRESSURE, SensorType.TEMPERATURE, SensorType.HUMIDITY)
    var pressure = 0.0
    var temp = 0.0
    var humidity = 0.0
}

