package fr.coppernic.tools.transparent.terminal

interface TerminalView {

    enum class Error {
        OK,
        OPEN_ERROR,
        INCORRECT_DATA_TO_SEND,
        PORT_NOT_OPENED
    }

    fun addLog(log:String)
    fun showError(error:Error)
}