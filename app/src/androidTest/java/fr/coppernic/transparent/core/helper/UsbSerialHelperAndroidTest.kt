package fr.coppernic.transparent.core.helper

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class UsbSerialHelperAndroidTest {

    @Before
    fun setUp() {
        Timber.plant(Timber.DebugTree())
    }

    @After
    fun tearDown() {
    }

    @Test
    fun listAllUsbSerialDevices() {
    }
}