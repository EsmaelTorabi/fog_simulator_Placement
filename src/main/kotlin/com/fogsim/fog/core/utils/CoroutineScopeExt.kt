package com.fogsim.fog.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


fun CoroutineScope.launchPeriodicAsync(
    repeatMillis: Long,
    action: () -> Unit
) = this.async {
    if (repeatMillis > 0) {
        while (isActive) {
            action()
            delay(repeatMillis)
        }
    } else {
        action()
    }
}

class Debouncer {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val delayedMap = ConcurrentHashMap<Any, Future<*>>()

    /**
     * Debounces `callable` by `delay`, i.e., schedules it to be executed after `delay`,
     * or cancels its execution if the method is called with the same key within the `delay` again.
     */
    fun debounce(key: Any, runnable: Runnable, delay: Long, unit: TimeUnit?) {
        val prev = delayedMap.put(key, scheduler.schedule({
            try {
                runnable.run()
            } finally {
                delayedMap.remove(key)
            }
        }, delay, unit))
        prev?.cancel(true)
    }

    fun shutdown() {
        scheduler.shutdownNow()
    }
}