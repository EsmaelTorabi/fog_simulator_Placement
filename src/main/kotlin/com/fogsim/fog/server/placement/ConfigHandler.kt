package com.fogsim.fog.server.placement

import com.fogsim.fog.core.utils.DataUtils
import com.fogsim.fog.fogDevice.FogDevice
import java.util.*
import kotlin.math.abs

class ConfigHandler {
    fun getUpdatedTupleConfigs(dataId: String, device: FogDevice): List<Double> {
        val data = device.controller!!.dataHandler.getRawDataList().first { dataId == it.id }
        val value = DataUtils.dataSizeInBytes(data.value)
        val config = device.config

        if (value > config.minDataSize || data.config.a1 > 1) {
            data.config.a1 = reduceEpsilonFrom(data.config.a1, "a",device)
        } else {
            data.config.a1 = addEpsilonTo(data.config.a1, "a",device)
        }

        if (device.controller!!.resourceLogger.emptySpace / device.storage< config.minStoragePercent || data.config.a2 > 1) {
            data.config.a2 = reduceEpsilonFrom(data.config.a2, "a",device)
        } else {
            data.config.a2 = addEpsilonTo(data.config.a2, "a",device)
        }

        if ((device.config.flu > config.minFlue && data.config.a3 > 0.1) || data.config.a3 > 1) {
            data.config.a3 = reduceEpsilonFrom(data.config.a3, "a",device)
        } else {
            data.config.a3 = addEpsilonTo(data.config.a3, "a",device)
        }

        if ((device.controller!!.resourceLogger.queueCount > config.minQueue) || data.config.a4 < 1) {
            data.config.a4 = addEpsilonTo(data.config.a4, "a",device)

        } else {
            data.config.a4 = reduceEpsilonFrom(data.config.a4, "a",device)
        }

        if ((data.expireTime > config.minExpireTime) || data.config.a5 < 1) {
            data.config.a5 = addEpsilonTo(data.config.a5, "a",device)
        } else {
            data.config.a5 = reduceEpsilonFrom(data.config.a5, "a",device)

        }

        return listOf(
            data.config.a1,
            data.config.a2,
            data.config.a3,
            data.config.a4,
            data.config.a5
        )
    }

    fun getUpdatedDeviceConfigs(device: FogDevice): List<Double> {
        val config = device.config

        val cTime = device.createdAt
        val currentTime = Date().time
        if (currentTime - cTime > 20000 && config.db1 < 1) {
            device.config.db1 = addEpsilonTo(config.db1, "b",device)
        } else if (config.db1 > 0.1) {
            device.config.db1 = reduceEpsilonFrom(config.db1, "b",device)
        }

        if (config.devicesPresenceVariance < 5 || config.db2 > 1) {
            device.config.db2 = reduceEpsilonFrom(config.db2, "b",device)
        } else {
            device.config.db2 = addEpsilonTo(config.db2, "b",device)
        }


        if (device.controller!!.resourceLogger.emptySpace / device.storage.toDouble() < 0.6 || config.db3 > 1) {
            device.config.db3 = reduceEpsilonFrom(config.db3, "b",device)
        } else {
            device.config.db3 = addEpsilonTo(config.db3, "b",device)
        }


        if (device.controller!!.resourceLogger.queueCount < 2 || config.db4 > 1) {
            device.config.db4 = reduceEpsilonFrom(config.db4, "b",device)
        } else {
            device.config.db4 = addEpsilonTo(config.db4, "b",device)
        }

        if (device.controller!!.resourceLogger.queueCount > 2 || config.db5 > 1) {
            device.config.db5 = reduceEpsilonFrom(config.db5, "b",device)
        } else {
            device.config.db5 = addEpsilonTo(config.db5, "b",device)
        }


        return mutableListOf(device.config.db1, device.config.db2, device.config.db3, device.config.db4, device.config.db5)
    }

    fun getUpdatedTaskConfigs(device: FogDevice): List<Double> {
        val config = device.config
        val logger = device.controller!!.resourceLogger
        if (logger.totalBusyTime / 1000 < 0.6 && config.tb1 < 1) {
            device.config.tb1 = addEpsilonTo(config.tb1, "t",device)
        } else {
            device.config.tb1 = reduceEpsilonFrom(config.tb1, "t",device)
        }

        if (logger.queueCount < 2 || config.tb2 > 1) {
            device.config.tb2 = reduceEpsilonFrom(config.tb2, "t",device)
        } else {
            device.config.tb2 = addEpsilonTo(config.tb2, "t",device)
        }

        if (logger.queueCount > 2 || config.tb3 > 1) {
            device.config.tb3 = reduceEpsilonFrom(config.tb3, "t",device)
        } else {
            device.config.tb3 = addEpsilonTo(config.tb3, "t",device)
        }

        return mutableListOf(device.config.tb1, device.config.tb2, device.config.tb3)
    }
    private fun addEpsilonTo(a: Double, type: String,device: FogDevice): Double {
        return when (type) {
            "a" -> {
                a + device.config.aEpsilon
            }
            "b" -> {
                a + device.config.bEpsilon
            }
            else -> {
                a + device.config.tEpsilon
            }
        }

    }

    private fun reduceEpsilonFrom(a: Double, type: String,device: FogDevice): Double {
        return when (type) {
            "a" -> {
                device.config.aEpsilon /= 2
                if ( device.config.aEpsilon >= a) {
                    device.config.aEpsilon = abs(a - 0.001)
                }
                a -  device.config.aEpsilon

            }
            "b" -> {
                device.config.bEpsilon /= 2
                if ( device.config.bEpsilon >= a) {
                    device.config.bEpsilon = abs(a - 0.001)
                }
                a -  device.config.bEpsilon
            }
            else -> {
                device.config.tEpsilon /= 2
                if ( device.config.tEpsilon >= a) {
                    device.config.tEpsilon = abs(a - 0.001)
                }
                a -  device.config.tEpsilon
            }
        }


    }



}
