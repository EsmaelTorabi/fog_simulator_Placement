package ir.fog.app.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


class EventEmitter(val coroutineScope: CoroutineScope){

    var baseEvent = MutableSharedFlow<BaseEvent>()

    fun emit(event: BaseEvent) {
        coroutineScope.launch {
            baseEvent.emit(event)
        }

    }
}