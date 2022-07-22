package fr.coppernic.tools.transparent.terminal

import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.serial.SerialCom.*
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

class TerminalPresenterImpl @Inject constructor():TerminalPresenter {

    @Inject
    lateinit var settings: SettingsInteractor

    private lateinit var view: TerminalView
    private var serial: SerialCom? = null
    private val subject:BehaviorSubject<Boolean> = BehaviorSubject.create()

    lateinit var observable: Observable<ByteArray>
    lateinit var observableEmitter: ObservableEmitter<ByteArray>
    var disposable:Disposable? = null

    private val observableOnSubcribe = ObservableOnSubscribe<ByteArray> {
        observableEmitter = it

        val availableData = serial?.queueStatus

        if (availableData != null && availableData > 0) {
            val temp = ByteArray(availableData)
            serial?.receive(100, availableData, temp)
            observableEmitter.onNext(temp)
        }
        if (disposable?.isDisposed == true) {
            observableEmitter.onComplete()
        } else {
            throw Exception()
        }
    }

    override fun setUp(view: TerminalView): BehaviorSubject<Boolean> {
        this.view = view

        return subject
    }

    override fun dispose() {

    }

    override fun setPort(serialPort: SerialCom) {
        serial = serialPort

        serial.let {
            it?.setRts(settings.getPortRts())
            it?.setXonXoff(settings.getPortXonXoff())
            it?.setHardwareFlowControl(settings.getPortHardwareFlowControl())
        }

        subject.onNext(true)
    }

    override fun openPort(name: String, baudrate: Int) {
        var ret: Int = -1
        serial?.let{
            ret = it.open(name, baudrate)
        }

        if (ret != 0) {
            when (ret) {
                ERROR_FAIL -> view.addLog("ERROR_FAIL")
                ERROR_OPEN -> view.addLog("ERROR_OPEN")
                ERROR_TIMEOUT -> view.addLog("ERROR_TIMEOUT")
                ERROR_OPEN_SECURITY_EXCEPTION -> view.addLog("ERROR_OPEN_SECURITY_EXCEPTION")
                ERROR_OPEN_IO_EXCEPTION -> view.addLog("ERROR_OPEN_IO_EXCEPTION")
                ERROR_NOT_BOUND -> view.addLog("ERROR_NOT_BOUND")
                ERROR_ALREADY_OPENED -> view.addLog("ERROR_ALREADY_OPENED")
            }
            view.showError(TerminalView.Error.OPEN_ERROR)
        } else {
            observable = Observable.create(observableOnSubcribe)
            observable.subscribeOn(Schedulers.newThread())
                    .retry()
                    .subscribe(object: Observer<ByteArray> {
                        override fun onSubscribe(d: Disposable) {
                            disposable = d
                        }

                        override fun onNext(t: ByteArray) {
                            view.addLog("<< " + CpcBytes.byteArrayToString(t))
                        }

                        override fun onError(e: Throwable) {

                        }

                        override fun onComplete() {

                        }
                    })
        }
    }

    override fun closePort() {
        serial.let{
            it?.close()
            disposable?.dispose()
        }
    }

    override fun send(data: String) {
        serial?.let { port ->
            val dataBytes:ByteArray?
            if (!port.isOpened) {
                view.showError(TerminalView.Error.PORT_NOT_OPENED)
                return
            }
            try {
                dataBytes = when(settings.getCommunicationAscii()) {
                    true -> {
                        var dataStr = data
                        when (settings.getCommunicationSuffix()) {
                            "[CR]" -> dataStr += "\r"
                            "[LF]" -> dataStr += "\n"
                            "[CR][LF]" -> dataStr += "\r\n"
                        }

                        dataStr.toByteArray(Charsets.UTF_8)
                    }
                    false -> {
                            CpcBytes.parseHexStringToArray(data)
                    }
                }
            } catch (ex:Exception) {
                view.showError(TerminalView.Error.INCORRECT_DATA_TO_SEND)
                return
            }

            dataBytes?.let { port.send(it, it.size) }
            view.addLog(">> $data")
        }
    }
}