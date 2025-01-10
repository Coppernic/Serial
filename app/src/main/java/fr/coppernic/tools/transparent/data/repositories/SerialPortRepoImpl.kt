package fr.coppernic.tools.transparent.data.repositories

import fr.coppernic.tools.transparent.domain.models.SerialPort
import fr.coppernic.tools.transparent.domain.models.SerialPortComm
import fr.coppernic.tools.transparent.domain.models.SerialPortPhysical
import fr.coppernic.tools.transparent.domain.models.SerialPortUsb
import fr.coppernic.tools.transparent.domain.repositories.SerialPortRepo
import fr.coppernic.tools.transparent.domain.sources.PhysicalSerialPortDataSource
import fr.coppernic.tools.transparent.domain.sources.UsbSerialPortDataSource

class SerialPortRepoImpl(
    private val physicalSerialPortDataSource: PhysicalSerialPortDataSource,
    private val usbSerialPortDataSource: UsbSerialPortDataSource
) : SerialPortRepo {

    override suspend fun getSerialPortFromReference(reference: String): SerialPort? {
        return getSerialPortList().firstOrNull {
            when (it) {
                is SerialPortUsb -> it.usbReference == reference
                is SerialPortPhysical -> it.physicalReference == reference
                else -> false
            }
        }
    }

    override suspend fun getSerialPortList(): List<SerialPort> {
        return physicalSerialPortDataSource.getSerialPortList() + usbSerialPortDataSource.getSerialPortList()
    }

    override fun getSerialPortReference(serialPort: SerialPort): String {
        return when (serialPort) {
            is SerialPortUsb -> serialPort.usbReference
            is SerialPortPhysical -> serialPort.physicalReference
            else -> { throw Exception("Unkown serial port type") }
        }
    }

    override suspend fun openSerialPort(
        serialPort: SerialPort,
        baudrate: Int,
        rts: Boolean,
        xonXoff: Boolean,
        hardwareFlowControl: Boolean
    ): SerialPortComm {
        return when (serialPort) {
            is SerialPortUsb -> usbSerialPortDataSource.openSerialPort(serialPort, baudrate, rts, xonXoff, hardwareFlowControl)
            is SerialPortPhysical -> physicalSerialPortDataSource.openSerialPort(serialPort, baudrate, rts, xonXoff, hardwareFlowControl)
            else -> { throw Exception("Unkown serial port type") }
        }
    }

    override suspend fun sendData(serialPortComm: SerialPortComm, data: ByteArray) {
        when (serialPortComm.serialPort) {
            is SerialPortUsb -> usbSerialPortDataSource.sendData(serialPortComm, data)
            is SerialPortPhysical -> physicalSerialPortDataSource.sendData(serialPortComm, data)
            else -> { throw Exception("Unkown serial port type") }
        }
    }

    override suspend fun receiveData(serialPortComm: SerialPortComm): ByteArray {
        return when (serialPortComm.serialPort) {
            is SerialPortUsb -> usbSerialPortDataSource.receiveData(serialPortComm)
            is SerialPortPhysical -> physicalSerialPortDataSource.receiveData(serialPortComm)
            else -> { throw Exception("Unkown serial port type") }
        }
    }

    override suspend fun closeSerialPort(serialPortComm: SerialPortComm) {
        when (serialPortComm.serialPort) {
            is SerialPortUsb -> usbSerialPortDataSource.closeSerialPort(serialPortComm)
            is SerialPortPhysical -> physicalSerialPortDataSource.closeSerialPort(serialPortComm)
            else -> { throw Exception("Unkown serial port type") }
        }
    }
}
