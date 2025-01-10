package fr.coppernic.tools.transparent.domain.sources

import fr.coppernic.tools.transparent.domain.models.SerialPortComm
import fr.coppernic.tools.transparent.domain.models.SerialPortUsb

interface UsbSerialPortDataSource {
    fun getSerialPortList(): List<SerialPortUsb>
    suspend fun openSerialPort(serialPort: SerialPortUsb, baudrate: Int, rts: Boolean, xonXoff: Boolean, hardwareFlowControl: Boolean): SerialPortComm
    fun sendData(serialPortComm: SerialPortComm, data: ByteArray)
    fun receiveData(serialPortComm: SerialPortComm): ByteArray
    fun closeSerialPort(serialPortComm: SerialPortComm)
}
