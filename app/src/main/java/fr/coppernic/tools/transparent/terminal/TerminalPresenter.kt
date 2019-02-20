package fr.coppernic.tools.transparent.terminal

import fr.coppernic.sdk.serial.SerialCom
import io.reactivex.subjects.BehaviorSubject

interface TerminalPresenter {
    fun setUp(view: TerminalView): BehaviorSubject<Boolean>
    fun dispose()
    fun setPort(serialPort:SerialCom)
    fun openPort(name:String, baudrate:Int)
    fun closePort()
    fun send(data:String)
}