package fr.coppernic.transparent.core.helper

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.coppernic.tools.transparent.core.helper.UsbSerialHelper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class UsbSerialHelperAndroidTest {

    private lateinit var context: Context
    private lateinit var usbSerialHelper: UsbSerialHelper


    @Before
    fun setUp() {
        Timber.plant(Timber.DebugTree())
        context = ApplicationProvider.getApplicationContext()
        usbSerialHelper = UsbSerialHelper(context)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun listAllUsbSerialDriver() {

        val usbserialList = usbSerialHelper.listAllUsbSerialDriver()

        Timber.d("usbserialList = $usbserialList")

    }


    @Test
    fun listAllUsbSerialPorts() {

        val usbserialList = usbSerialHelper.listAllUsbSerialPorts()

        Timber.d("usbserialList = $usbserialList")

    }

}