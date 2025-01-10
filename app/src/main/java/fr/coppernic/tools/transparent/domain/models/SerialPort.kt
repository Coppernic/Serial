package fr.coppernic.tools.transparent.domain.models

import com.hoho.android.usbserial.driver.UsbSerialPort
import fr.coppernic.sdk.serial.SerialCom

abstract class SerialPort

data class SerialPortPhysical(
    val physicalReference: String,
): SerialPort()

data class SerialPortUsb(
    val usbReference: String,
): SerialPort()


data class SerialPortComm(
    val serialPort: SerialPort,
    val serialCom: SerialCom? = null,
    val usbSerialPort: UsbSerialPort? = null,
)
