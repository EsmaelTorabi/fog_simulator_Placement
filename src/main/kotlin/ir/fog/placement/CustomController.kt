package ir.fog.placement

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import ir.fog.core.EventBus
import ir.fog.app.broker.CustomBroker
import ir.fog.app.device.CustomFogDevice
import ir.fog.app.sensor.CustomSensor
import ir.fog.app.device.Device
import ir.fog.app.server.Server
import java.util.concurrent.TimeUnit

class CustomController(var broker: CustomBroker, var deviceList: List<Device>, var sensors: List<CustomSensor>) {

     var maxSimulationTime: Long = 30

    private var fogDevices: MutableList<CustomFogDevice> = mutableListOf()
    var disposable = CompositeDisposable()

    init {

        for (device in deviceList) {
            when (device) {
                is CustomFogDevice -> {

                    fogDevices.add(device)
                }
            }
        }

        for (device in deviceList) {
            if (device is Server) {
                device.availableDevices = fogDevices
            }
        }
    }


    fun start() {
        broker.startEntity()

        for (device in deviceList) {
            device.startEntity()
        }
        for (sensor in sensors) {
            sensor.startEntity()
        }


        EventBus.start()
        startTimer()
    }


    private fun startTimer() {

        disposable.add(
            Observable.interval(0,1,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                println("PassedTime $it")
            }
            .takeUntil{ it == (maxSimulationTime) }
            .filter{it >= maxSimulationTime}
            .subscribe({
                println("Stopppppppped $it")
                EventBus.stop()
            }){
                    obj: Throwable -> obj.printStackTrace()
            })

    }
}
