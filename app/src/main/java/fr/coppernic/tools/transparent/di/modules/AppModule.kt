package fr.coppernic.tools.transparent.di.modules

import android.content.SharedPreferences
import fr.coppernic.tools.transparent.core.helper.UsbSerialHelper
import fr.coppernic.tools.transparent.data.sources.UsbSerialPortDataSourceImpl
import fr.coppernic.tools.transparent.data.sources.PhysicalSerialPortDataSourceImpl
import fr.coppernic.tools.transparent.domain.sources.PhysicalSerialPortDataSource
import fr.coppernic.tools.transparent.domain.sources.UsbSerialPortDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import android.preference.PreferenceManager

val androidModule by lazy {
    module {
        single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(get()) }
    }
}
