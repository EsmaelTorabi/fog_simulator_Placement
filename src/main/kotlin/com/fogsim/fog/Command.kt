package com.fogsim.fog

import com.fogsim.fog.cluster.Cluster
import com.fogsim.fog.core.models.DataPlacementType
import com.fogsim.fog.core.models.Task
import com.fogsim.fog.core.models.TaskPlacementType

open class Command(
    val type: CommandType,
    val clusterList: List<Cluster>?,
    var dataPlacementType: DataPlacementType = DataPlacementType.CUSTOM_CA_REPLICA,
    var taskPlacementType: TaskPlacementType = TaskPlacementType.CUSTOM_PERFORMANCE_AWARE,
    var maxSimulationTime: Int = 60
)

enum class CommandType {
    START,
    STOP,
    RESET
}
