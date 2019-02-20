package fr.coppernic.tools.transparent.transparent

import fr.coppernic.sdk.serial.SerialCom
import io.reactivex.subjects.BehaviorSubject

interface TransparentPresenter {
    fun setUp(view: TransparentView):BehaviorSubject<Boolean>
    fun dispose()
    fun setPort(port: TransparentView.Port, serialCom: SerialCom?)
    fun openPorts(nameIn:String, baudrateIn:Int, nameOut:String, baudrateOut:Int)
    fun closePorts()
}