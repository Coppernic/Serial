package fr.coppernic.tools.transparent.core.helper

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber


class UsbSerialHelper(
    val context: Context
) {
    fun listAllUsbSerialPorts(): List<UsbSerialPort> {
        return ArrayList<UsbSerialPort>().apply {
            listAllUsbSerialDriver().map { usbSerialDriver ->
                usbSerialDriver.ports.map { usbSerialPort ->
                    add(usbSerialPort)
                }
            }
        }
    }

    fun listAllUsbSerialDriver(): List<UsbSerialDriver> {
        // Find all available drivers from attached devices.
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        return availableDrivers ?: listOf()
    }
}