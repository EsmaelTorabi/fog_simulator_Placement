package ir.fog.app.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val appModule = module {
    single {CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::EventEmitter)
}