package ir.fog.app.device

/**
 * @author mohsen on 1/13/23
 */
data class FogDeviceState(
    val isStarted: Boolean = false,
    var isStopped: Boolean = false,
    var isFinished: Boolean = false,
    var isPaused: Boolean = false,
    var isResumed: Boolean = false
) {
    companion object {
        val initial = FogDeviceState()
    }

    fun build(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(state: FogDeviceState) {
        var isStarted: Boolean = state.isStarted
        var isStopped: Boolean = state.isStopped
        var isFinished: Boolean = state.isFinished
        var isPaused: Boolean = state.isPaused
        var isResumed: Boolean = state.isResumed

        fun build() = FogDeviceState(
            isStarted = isStarted,
            isStopped = isStopped,
            isFinished = isFinished,
            isPaused = isPaused,
            isResumed = isResumed
        )
    }
}

