package fr.coppernic.tools.transparent.data.sources

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import fr.coppernic.sdk.utils.core.CpcBytes
import fr.coppernic.tools.transparent.core.helper.UsbPermission
import fr.coppernic.tools.transparent.core.helper.UsbSerialHelper
import fr.coppernic.tools.transparent.domain.models.SerialPortComm
import fr.coppernic.tools.transparent.domain.models.SerialPortUsb
import fr.coppernic.tools.transparent.domain.sources.UsbSerialPortDataSource
import timber.log.Timber

class UsbSerialPortDataSourceImpl(
    val usbSerialHelper: UsbSerialHelper,
    val context: Context,
) : UsbSerialPortDataSource {

    var usbReferenceMap = HashMap<String, UsbSerialPort>()

    override fun getSerialPortList(): List<SerialPortUsb> {
        usbReferenceMap.clear()
        return usbSerialHelper.listAllUsbSerialPorts().map { usbSerialPort ->
            Timber.d("usbSerialPort = $usbSerialPort")

            val usbDriverName = usbSerialPort.driver::class.simpleName?.replace("Driver", "")
            val usbReference = "USB-${usbDriverName}-${usbSerialPort.portNumber}"

            Timber.d("${usbSerialPort.driver.device.deviceName}-${usbSerialPort.portNumber}".replace("/dev/bus/",""))
            val serialPortUsb = SerialPortUsb(usbReference)
            usbReferenceMap.put(usbReference, usbSerialPort)
            serialPortUsb
        }
    }

    override suspend fun openSerialPort(
        serialPort: SerialPortUsb,
        baudrate: Int,
        rts: Boolean,
        xonXoff: Boolean,
        hardwareFlowControl: Boolean
    ): SerialPortComm {
        val usbSerialPort = usbReferenceMap.get(serialPort.usbReference)
            ?: throw Exception("Unknown UsbSerialPort for reference ${serialPort.usbReference}")
        val usbSerialDriver = usbSerialPort.driver

        // Check that permission is allowed
        UsbPermission.requestPermission(context, usbSerialPort.device, true)

        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?

        val usbDeviceConnection = manager?.openDevice(usbSerialDriver.device)
        usbSerialPort.open(usbDeviceConnection)

        usbSerialPort.setParameters(baudrate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        usbSerialPort.rts = rts
        // TODO : rts and xonXoff

        return SerialPortComm(
            serialPort, null, usbSerialPort
        )
    }

    override fun sendData(serialPortComm: SerialPortComm, data: ByteArray) {
        Timber.d("sendData with ${CpcBytes.byteArrayToAsciiString(data)}")
        serialPortComm.usbSerialPort?.write(data, 200)
        Timber.d("after sendData")
    }

    override fun receiveData(serialPortComm: SerialPortComm): ByteArray {
        val response = ByteArray(100)

        val len = serialPortComm.usbSerialPort?.read(response, 100)
        return if (len != null && len > 0) {
            response.copyOfRange(0, len)
        } else {
            byteArrayOf()
        }
    }

    override fun closeSerialPort(serialPortComm: SerialPortComm) {
        serialPortComm.usbSerialPort?.close()
    }
}
