package com.fogsim.fog.fogDevice

import com.fogsim.fog.core.models.*
import com.fogsim.fog.core.utils.DataUtils

class FogDeviceDataHandler(private val resourceLogger: DeviceResourceLogger) {
    private val rawDataList = mutableListOf<Data>()
    private val analyzedDataList = mutableListOf<AnalyzedData>()
    fun handleRawData(data: Data) {
        val typeList = rawDataList.map { it.id }
        if (typeList.contains(data.id)) {

            val index = typeList.indexOf(data.id)
            val pSize = DataUtils.dataSizeInBytes(rawDataList[index].value)
            val cSize = DataUtils.dataSizeInBytes(data.value)
            if (pSize > cSize) {
                resourceLogger.increaseEmptySpace((pSize - cSize).toDouble())
            } else {
                resourceLogger.reduceEmptySpace((cSize - pSize).toDouble())
            }
            rawDataList[index] = data
        } else {
            resourceLogger.reduceEmptySpace(DataUtils.dataSizeInBytes(data.value).toDouble())
            rawDataList.add(data)
        }
    }

    fun handleAnalyzedData(data: AnalyzedData) {
        val typeList = analyzedDataList.map { it.id }
        if (typeList.contains(data.id)) {
            val index = typeList.indexOf(data.id)
            analyzedDataList[index] = data
        } else {
            analyzedDataList.add(data)
        }
    }

    fun getRawDataList(): List<Data> {
        val returnList = mutableListOf<Data>()
        returnList.addAll(rawDataList)
        return returnList
    }

    fun getRawDataById(id: String): Data {
        try {
            return rawDataList.first { it.id == id }
        } catch (e: Exception) {
            throw Exception("Data with id $id not found")
        }
        return rawDataList.first { it.id == id }
    }

    fun changeDataAnalysisState(data: Data, state: TaskAnalyzeState) {
        if (rawDataList.contains(data)) {
            val index = rawDataList.indexOf(data)
            rawDataList[index].analyzeState = state
        }
    }

    fun getDataConfigs(id: String): List<Double> {
        val data = rawDataList.first { it.id == id }
        return listOf(data.config.a1, data.config.a2, data.config.a3, data.config.a4, data.config.a5)
    }
    fun updateDataConfigs(id: String, config: DataConfig) {
       rawDataList.firstOrNull { it.id == id }?.config = config
    }

    fun removeAnalyzedData(data: AnalyzedData) {
        if (analyzedDataList.contains(data)) {
            analyzedDataList.remove(data)
        }
    }

    fun getAnalyzedDataList(): List<AnalyzedData> {
        val returnList = mutableListOf<AnalyzedData>()
        returnList.addAll(analyzedDataList)
        return returnList
    }

    fun provideRequestedData(type: SensorType): AnalyzedData? {
        return analyzedDataList.firstOrNull { it.sensorType == type }
    }


}