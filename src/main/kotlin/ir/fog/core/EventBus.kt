package ir.fog.core

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

object EventBus {

    private var eventSubject = PublishSubject.create<FogEvent>()

    fun sendEvent(event: FogEvent) {
        eventSubject.onNext(event)

    }

    fun observe(): Observable<FogEvent> = eventSubject.ofType(FogEvent::class.java)


    fun restart() {
        eventSubject = PublishSubject.create()
    }

    fun start() {

        eventSubject.onNext(
            FogEvent(
                "EVENTBUS",
                0.0,
                eventType = SimEvents.START_SIMULATION,
                currentEntityId = "EVENTBUS",
                nextEntity = "",
                data = "start",
            )
        )
    }

    fun destroy() {
        eventSubject.onComplete()
    }

    fun stop() {
        eventSubject.onNext(
            FogEvent(
                "Central System",
                500.0,
                SimEvents.STOP_EXECUTION,
                "",
                currentEntityId = "Central System",
                nextEntity = ""
            )
        )
    }
}
