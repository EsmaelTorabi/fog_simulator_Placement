package ir.fog.app.gateway

/**
 * @author mohsen on 1/13/23
 */
data class GatewayState(
    val isStarted: Boolean = false,
    var isStopped: Boolean = false,
    var isFinished: Boolean = false,
    var isPaused: Boolean = false,
    var isResumed: Boolean = false
) {
    companion object {
        val initial = GatewayState()
    }

    fun build(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(state: GatewayState) {
        var isStarted: Boolean = state.isStarted
        var isStopped: Boolean = state.isStopped
        var isFinished: Boolean = state.isFinished
        var isPaused: Boolean = state.isPaused
        var isResumed: Boolean = state.isResumed

        fun build() = GatewayState(
            isStarted = isStarted,
            isStopped = isStopped,
            isFinished = isFinished,
            isPaused = isPaused,
            isResumed = isResumed
        )
    }
}

