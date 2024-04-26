package com.fogsim.fog

import com.fogsim.fog.core.AppCoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object EventEmitter {
    private val baseEvent = MutableSharedFlow<MainEvent>()
    private val scope = AppCoroutineScope()
    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }
    var event = baseEvent.asSharedFlow()
    fun emit(event: MainEvent) {
        scope.launch(coroutineExceptionHandler) {
            baseEvent.emit(event)
        }
    }

    fun emit(event: MainEvent, delay: Long) {
        scope.launch(coroutineExceptionHandler) {
            kotlinx.coroutines.delay(delay)
            baseEvent.emit(event)
        }
    }

}