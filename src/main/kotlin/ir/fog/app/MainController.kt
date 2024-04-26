package ir.fog.app

import com.fogsim.fog.core.AppCoroutineScope
import ir.fog.core.FogEvent
import ir.fog.core.SimEvents
import ir.fog.core.ext.launchPeriodicAsync
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * @author mohsen on 1/13/23
 */
class MainController {
    private var _state: MutableStateFlow<AppState> = MutableStateFlow(AppState())
    var state: StateFlow<AppState> = _state

    private var finishJob: Job? = null


    fun startSimulation() {
        _state.value = _state.value.build { isStarted = true }
        handleEvents(
            AppEvent.StartSimulation(
                FogEvent(
                    "Central System", 500.0,
                    SimEvents.STOP_EXECUTION,
                    "",
                    currentEntityId = "Central System",
                    nextEntity = ""
                )
            )
        )
        finishJob = AppCoroutineScope().launchPeriodicAsync(_state.value.simulationTime * 1000L) {
            _state.value = _state.value.build {
                isFinished = true
                isStarted = false
            }
            stopExecution()
            finishJob?.cancel()
        }
        finishJob?.start()
    }

    fun stopSimulation() {
        stopExecution()
        finishJob?.cancel()
    }


    fun restartSimulation() {
        _state.value = _state.value.build { isResumed = true }
        stopExecution()
        startSimulation()
    }

    private fun stopExecution() {
        _state.value = _state.value.build { isStopped = true }
        handleEvents(
            AppEvent.StopExecution(
                FogEvent(
                    "Central System",
                    500.0,
                    SimEvents.STOP_EXECUTION,
                    "",
                    currentEntityId = "Central System",
                    nextEntity = ""
                )
            )
        )
    }

}


