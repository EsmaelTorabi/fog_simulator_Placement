package ir.fog

import ir.fog.app.app.appModule
import org.koin.core.context.startKoin


object Test2 {

    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            modules(appModule)
        }


    }
}
