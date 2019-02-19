package fr.coppernic.tools.transparent.home

import fr.coppernic.sdk.serial.SerialCom
import io.reactivex.subjects.BehaviorSubject

interface HomePresenter {
    fun setUp(view:HomeView):BehaviorSubject<Boolean>
    fun dispose()
    fun setPort(port: HomeView.Port, serialCom: SerialCom?)
    fun openPorts(nameIn:String, baudrateIn:Int, nameOut:String, baudrateOut:Int)
    fun closePorts()
}