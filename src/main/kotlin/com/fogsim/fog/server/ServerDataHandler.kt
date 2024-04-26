package com.fogsim.fog.server

import com.fogsim.fog.core.models.AnalyzedData
import com.fogsim.fog.core.models.Data
import com.fogsim.fog.core.models.DataConfig
import com.fogsim.fog.core.models.SensorType
import java.util.*

class ServerDataHandler {
    var dataSet: MutableList<Data> = mutableListOf()
    private var analyzedDataSet: MutableList<AnalyzedData> = mutableListOf()
    private var allData: MutableList<Data> = mutableListOf()
    var dataConfigListMap: MutableMap<String, DataConfig> = mutableMapOf()
    private var dataList: MutableList<String> = mutableListOf()
    private var sensorTypeDataListMap: EnumMap<SensorType, MutableList<Data>> = EnumMap(SensorType::class.java)

    fun analyzeData(data: Data) {
        if (dataConfigListMap.containsKey(data.id)) {
            data.config = dataConfigListMap[data.id]!!
        }
        allData.add(data)
        handleReceivedData(data)
    }

    private fun handleReceivedData(data: Data) {

        if (sensorTypeDataListMap.containsKey(data.sensorType)) {
            if (sensorTypeDataListMap[data.sensorType]!!.none { it.id == data.id }) {
                sensorTypeDataListMap[data.sensorType]!!.add(data)
            } else {
                val list = sensorTypeDataListMap[data.sensorType]!!
                val index = list.indexOfFirst { it.id == data.id }
                list[index] = data
                sensorTypeDataListMap[data.sensorType] = list
            }
        }else{
            sensorTypeDataListMap[data.sensorType] = mutableListOf(data)
        }

        if (dataSet.size == 0) {
            dataSet.add(data)
            dataList.add(data.id)
        } else {
            if (dataList.contains(data.id)) {
                val index = dataSet.indexOfFirst { it.sensorType == data.sensorType }
                dataSet[index] = data
            } else {
                dataList.add(data.id)
                dataSet.add(data)
            }
        }

    }

    fun handleAnalyzedData(data: AnalyzedData) {
        analyzedDataSet.add(data)
    }

    fun updateDataConfigListMap(id: String, config: DataConfig) {
        dataConfigListMap[id] = config
    }


    fun getDataListForSensorType(sensorType: SensorType): List<String> {
        return sensorTypeDataListMap[sensorType]?.map { it.id } ?: listOf()
    }

}