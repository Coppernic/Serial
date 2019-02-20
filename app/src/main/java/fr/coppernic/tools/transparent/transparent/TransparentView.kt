package fr.coppernic.tools.transparent.transparent

interface TransparentView {

    enum class Port {
        IN,
        OUT
    }

    fun addLog(log:String)

    enum class Error {
        OK,
        OPEN_ERROR_PORT_IN,
        OPEN_ERROR_PORT_OUT
    }

    fun showError(error: TransparentView.Error)
}