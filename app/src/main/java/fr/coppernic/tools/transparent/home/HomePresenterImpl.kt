package fr.coppernic.tools.transparent.home

import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.utils.core.CpcBytes
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class HomePresenterImpl @Inject constructor(): HomePresenter {
    private lateinit var view:HomeView
    private var serialIn: SerialCom? = null
    private var serialOut: SerialCom? = null
    private val subject:BehaviorSubject<Boolean> = BehaviorSubject.create()

    lateinit var observableIn: Observable<ByteArray>
    lateinit var observableEmitterIn:ObservableEmitter<ByteArray>

    val observableOnSubcribeIn = ObservableOnSubscribe<ByteArray> {
        observableEmitterIn = it

        val availableData = serialIn?.queueStatus

        if (availableData != null && availableData > 0) {
            val temp = ByteArray(availableData)
            serialIn?.receive(100, availableData, temp)
            serialOut?.send(temp, temp.size)
            view.addLog(">> " + CpcBytes.byteArrayToString(temp))
        }

        throw Exception()
    }

    lateinit var observableOut: Observable<ByteArray>
    lateinit var observableEmitterOut:ObservableEmitter<ByteArray>
    val observableOnSubcribeOut = ObservableOnSubscribe<ByteArray> {
        observableEmitterOut = it

        val availableData = serialOut?.queueStatus

        if (availableData != null && availableData > 0) {
            val temp = ByteArray(availableData)
            serialOut?.receive(100, availableData, temp)
            serialIn?.send(temp, temp.size)
            view.addLog("<< " + CpcBytes.byteArrayToString(temp))
        }

        throw Exception()
    }

    override fun setUp(view: HomeView):BehaviorSubject<Boolean> {
        this.view = view

        view.addPort(HomeView.Port.IN, "/dev/ttyHSL1")
        view.addPort(HomeView.Port.IN, "/dev/ttyUSB0")
        view.addPort(HomeView.Port.OUT, "/dev/ttyUSB0")
        view.addPort(HomeView.Port.OUT, "/dev/ttyHSL1")

        view.addBaudrate(HomeView.Port.IN, 115200)
        view.addBaudrate(HomeView.Port.IN, 9600)
        view.addBaudrate(HomeView.Port.IN, 19200)
        view.addBaudrate(HomeView.Port.IN, 38400)
        view.addBaudrate(HomeView.Port.IN, 57600)
        view.addBaudrate(HomeView.Port.IN, 230400)
        view.addBaudrate(HomeView.Port.IN, 460800)
        view.addBaudrate(HomeView.Port.OUT, 115200)
        view.addBaudrate(HomeView.Port.OUT, 9600)
        view.addBaudrate(HomeView.Port.OUT, 19200)
        view.addBaudrate(HomeView.Port.OUT, 38400)
        view.addBaudrate(HomeView.Port.OUT, 57600)
        view.addBaudrate(HomeView.Port.OUT, 230400)
        view.addBaudrate(HomeView.Port.OUT, 460800)

        return subject
    }

    override fun dispose() {

    }

    override fun setPort(port: HomeView.Port, serialCom: SerialCom?) {
        when (port) {
            HomeView.Port.IN -> {
                serialIn = serialCom
                observableIn = Observable.create(observableOnSubcribeIn)
                observableIn.subscribeOn(Schedulers.newThread())
                        .retry()
                        .subscribe(object:Observer<ByteArray>{
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(t: ByteArray) {

                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onComplete() {

                            }

                        })

            }
            HomeView.Port.OUT ->{
                serialOut = serialCom
                observableOut = Observable.create(observableOnSubcribeOut)
                observableOut.subscribeOn(Schedulers.newThread())
                        .retry()
                        .subscribe(object:Observer<ByteArray>{
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(t: ByteArray) {

                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onComplete() {

                            }

                        })

            }
        }

        val test = serialIn!= null && serialOut != null
        subject.onNext(test)
    }

    override fun openPorts(nameIn:String, baudrateIn:Int, nameOut:String, baudrateOut:Int) {
        serialIn?.open(nameIn, baudrateIn)
        serialOut?.open(nameOut, baudrateOut)
    }

    override fun closePorts() {
        serialIn?.close()
        serialOut?.close()
    }
}
