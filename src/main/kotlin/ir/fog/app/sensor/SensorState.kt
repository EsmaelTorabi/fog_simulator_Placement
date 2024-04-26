package ir.fog.app.sensor

import ir.fog.entities.TupleType

/**
 * @author mohsen on 1/13/23
 */
data class SensorState(
    val isStarted: Boolean = false,
    var isStopped: Boolean = false,
    var isFinished: Boolean = false,
    var isPaused: Boolean = false,
    var isResumed: Boolean = false,
    var lastTriggeredEvent: TupleType = TupleType.Task
) {
    companion object {
        val initial = SensorState()
    }

    fun build(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(state: SensorState) {
        var isStarted: Boolean = state.isStarted
        var isStopped: Boolean = state.isStopped
        var isFinished: Boolean = state.isFinished
        var isPaused: Boolean = state.isPaused
        var isResumed: Boolean = state.isResumed
        var lastTriggeredEvent: TupleType = state.lastTriggeredEvent

        fun build() = SensorState(
            isStarted = isStarted,
            isStopped = isStopped,
            isFinished = isFinished,
            isPaused = isPaused,
            isResumed = isResumed,
            lastTriggeredEvent = lastTriggeredEvent
        )
    }
}
