package fr.coppernic.tools.transparent.transparent

import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.utils.core.CpcBytes
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class TransparentPresenterImpl @Inject constructor() : TransparentPresenter {
    @Inject
    lateinit var settings: SettingsInteractor

    private lateinit var view: TransparentView
    private var serialIn: SerialCom? = null
    private var serialOut: SerialCom? = null
    private val subject: BehaviorSubject<Boolean> = BehaviorSubject.create()

    lateinit var observableIn: Observable<ByteArray>
    lateinit var observableEmitterIn: ObservableEmitter<ByteArray>
    lateinit var disposableIn: Disposable

    private val observableOnSubscribeIn = ObservableOnSubscribe<ByteArray> {
        observableEmitterIn = it

        val availableData = serialIn?.queueStatus

        if (availableData != null && availableData > 0) {
            val temp = ByteArray(availableData)
            serialIn?.receive(100, availableData, temp)
            it.onNext(temp)
        }

        if (disposableIn.isDisposed) {
            observableEmitterIn.onComplete()
        } else {
            // To retry
            throw Exception()
        }
    }

    lateinit var observableOut: Observable<ByteArray>
    lateinit var observableEmitterOut: ObservableEmitter<ByteArray>
    lateinit var disposableOut: Disposable

    private val observableOnSubscribeOut = ObservableOnSubscribe<ByteArray> {
        observableEmitterOut = it

        val availableData = serialOut?.queueStatus

        if (availableData != null && availableData > 0) {
            val temp = ByteArray(availableData)
            serialOut?.receive(100, availableData, temp)
            it.onNext(temp)
        }

        if (disposableOut.isDisposed) {
            observableEmitterOut.onComplete()
        } else {
            // To retry
            throw Exception()
        }
    }

    override fun setUp(view: TransparentView): BehaviorSubject<Boolean> {
        this.view = view

        return subject
    }

    override fun dispose() {

    }

    private fun startObserverIn() {
        observableIn = Observable.create(observableOnSubscribeIn)
        observableIn.subscribeOn(Schedulers.newThread())
                .retry()
                .subscribe(object : Observer<ByteArray> {
                    override fun onSubscribe(d: Disposable) {
                        disposableIn = d
                    }

                    override fun onNext(t: ByteArray) {
                        serialOut?.send(t, t.size)
                        view.addLog(">> " + CpcBytes.byteArrayToString(t))
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }
                })
    }

    private fun startObserverOut() {
        observableOut = Observable.create(observableOnSubscribeOut)
        observableOut.subscribeOn(Schedulers.newThread())
                .retry()
                .subscribe(object : Observer<ByteArray> {
                    override fun onSubscribe(d: Disposable) {
                        disposableOut = d
                    }

                    override fun onNext(t: ByteArray) {
                        serialIn?.send(t, t.size)
                        view.addLog("<< " + CpcBytes.byteArrayToString(t))
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }

                })

    }

    override fun setPort(port: TransparentView.Port, serialCom: SerialCom?) {
        when (port) {
            TransparentView.Port.IN -> {
                serialIn = serialCom
                serialIn.let {
                    it?.setRts(settings.getPortRts())
                    it?.setXonXoff(settings.getPortXonXoff())
                    it?.setHardwareFlowControl(settings.getPortHardwareFlowControl())
                }
            }

            TransparentView.Port.OUT -> {
                serialOut = serialCom
                serialOut.let {
                    it?.setRts(settings.getPortRts())
                    it?.setXonXoff(settings.getPortXonXoff())
                    it?.setHardwareFlowControl(settings.getPortHardwareFlowControl())
                }
            }
        }
        val test = serialIn != null && serialOut != null
        subject.onNext(test)
    }

    override fun openPorts(nameIn: String, baudrateIn: Int, nameOut: String, baudrateOut: Int) {
        serialIn.let {
            val ret = it?.open(nameIn, baudrateIn)

            if (ret != 0) {
                view.showError(TransparentView.Error.OPEN_ERROR_PORT_IN)
            } else {
                startObserverIn()
            }
        }
        serialOut.let {
            val ret = it?.open(nameOut, baudrateOut)

            if (ret != 0) {
                view.showError(TransparentView.Error.OPEN_ERROR_PORT_OUT)
            } else {
                startObserverOut()
            }
        }
    }

    override fun closePorts() {
        serialIn.let {
            it?.close()
            disposableIn.dispose()
        }
        serialOut.let {
            it?.close()
            disposableOut.dispose()
        }
    }
}
