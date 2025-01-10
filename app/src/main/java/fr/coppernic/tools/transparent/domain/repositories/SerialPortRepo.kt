package fr.coppernic.tools.transparent.domain.repositories

import fr.coppernic.tools.transparent.domain.models.SerialPort
import fr.coppernic.tools.transparent.domain.models.SerialPortComm

interface SerialPortRepo {
    suspend fun getSerialPortFromReference(reference: String): SerialPort?
    suspend fun getSerialPortList(): List<SerialPort>
    fun getSerialPortReference(serialPort: SerialPort): String
    suspend fun openSerialPort(serialPort: SerialPort, baudrate: Int, rts: Boolean, xonXoff: Boolean, hardwareFlowControl: Boolean): SerialPortComm
    suspend fun sendData(serialPortComm: SerialPortComm, data: ByteArray)
    suspend fun receiveData(serialPortComm: SerialPortComm): ByteArray
    suspend fun closeSerialPort(serialPortComm: SerialPortComm)
}
