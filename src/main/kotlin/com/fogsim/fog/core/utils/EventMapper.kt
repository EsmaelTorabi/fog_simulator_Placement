package com.fogsim.fog.core.utils

import com.fogsim.fog.MainEvent
import com.fogsim.fog.fogDevice.FogDeviceEvent
import com.fogsim.fog.server.ServerEvent

object EventMapper {


    fun map(event: MainEvent): String {

        return when (event) {
            is FogDeviceEvent -> {
                when (event) {
                    is FogDeviceEvent.Analyze -> {
                        getMainEvent(event) + """
                "dataId": "${event.dataId}"
                }
                """
                    }

                    is FogDeviceEvent.CatchRequestedData -> {
                        getMainEvent(event) + """
                "requestedData": ${event.requestedData.toJson()}
                }
                """
                    }

                    is FogDeviceEvent.HandleAnalyzedData -> {
                        getMainEvent(event) + """
                "analyzedData": ${event.analyzedData.toJson()}
                }
                """
                    }

                    is FogDeviceEvent.ProvideRequestedData -> {
                        getMainEvent(event) + """
                "type": "${event.type.name}",
                "targetDeviceId": "${event.targetDeviceId}",
                "targetDeviceLocation": ${event.targetDeviceLocation}
                }
                """
                    }

                    is FogDeviceEvent.RequestMissingData -> {
                        getMainEvent(event) + """
                "type": "${event.type.name}"
                }
                """
                    }

                    is FogDeviceEvent.SendAnalyzedData -> {
                        getMainEvent(event) + """
                "analyzedData": ${event.analyzedData.toJson()}
                }
                """
                    }

                    is FogDeviceEvent.SendWarning -> {
                        getMainEvent(event) + """
                "warning": ${event.warning.toJson()}
                }
                """
                    }

                    is FogDeviceEvent.UpdateDataConfig -> {
                        getMainEvent(event) + """
                "dataId": "${event.dataId}",
                "dataConfig": ${event.dataConfig.toJson()}
                }
                """
                    }

                    is FogDeviceEvent.UpdateDeviceConfig -> {
                        getMainEvent(event) + """
                "deviceConfig": ${event.deviceConfig},
                "taskConfig": ${event.taskConfig}
                }
                """
                    }
                }
            }

            is ServerEvent -> {
                when (event) {
                    ServerEvent.CheckAllTasksFinished -> {
                        getMainEvent(event) + """}"""

                    }

                    is ServerEvent.DecreaseNumberOfCopies -> {
                        getMainEvent(event) + """
                "targetData":${event.targetData},
                "count":${event.count}
                }
                """
                    }

                    is ServerEvent.HandleAnalyzedData -> {
                        getMainEvent(event) + """
                        "analyzedData":${event.analyzedData.toJson()}
                        }
                        """
                    }

                    is ServerEvent.HandleWarning -> {
                        getMainEvent(event) + """
                        "warning":${event.warning.toJson()}
                        }
                        """
                    }

                    is ServerEvent.IncreaseNumberOfCopies -> {
                        getMainEvent(event) + """
                        "targetData":${event.targetData},
                        "count":${event.count}
                        }
                        """
                    }

                    is ServerEvent.RequestMissingData -> {
                        getMainEvent(event) + """
                        "type": "${event.type.name}",
                        "deviceId": "${event.deviceId}",
                        "deviceLocation": ${event.deviceLocation}
                        }
                        """
                    }

                    is ServerEvent.SendTaskToDevice -> {
                        getMainEvent(event) + """
                        "deviceId": "${event.deviceId}"
                        }
                        """
                    }

                    is ServerEvent.TransferToDevice -> {
                        getMainEvent(event) + """
                        "deviceId": "${event.deviceId}",
                        "requestedData": ${event.requestedData.toJson()}
                        }
                        """
                    }

                    is ServerEvent.UpdateDataConfig -> {
                        getMainEvent(event) + """
                        "dataId": "${event.dataId}",
                        "dataConfig": ${event.dataConfig.toJson()}
                        }
                        """
                    }

                    is ServerEvent.UpdateDevice -> {
                        getMainEvent(event) + """
                        "deviceId": ${event.device.id}
                        }
                        """
                    }
                }
            }

            else -> {
                event.toJson()
            }
        }
    }


    private fun getMainEvent(event: MainEvent): String {
        return """
            {
                "id": "${event.id}",
                "data": ${event.data.toJson()},
                "senderId": "${event.senderId}",
                "receiverId": "${event.receiverId}",
                "delay": ${event.delay},
                "date": ${event.date},
                "eventType": "${event.eventType}",
            
        """.trimIndent()
    }
}