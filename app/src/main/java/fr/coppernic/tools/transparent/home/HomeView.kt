package fr.coppernic.tools.transparent.home

interface HomeView {

    enum class Port {
        IN,
        OUT
    }

    fun addPort(port:Port, name:String)
    fun addBaudrate(port:Port, baudrate:Int)
    fun addLog(log:String)
}