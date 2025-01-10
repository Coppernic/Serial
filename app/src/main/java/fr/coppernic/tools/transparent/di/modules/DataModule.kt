package fr.coppernic.tools.transparent.di.modules

import fr.coppernic.tools.transparent.core.helper.UsbSerialHelper
import fr.coppernic.tools.transparent.data.sources.UsbSerialPortDataSourceImpl
import fr.coppernic.tools.transparent.data.sources.PhysicalSerialPortDataSourceImpl
import fr.coppernic.tools.transparent.domain.sources.PhysicalSerialPortDataSource
import fr.coppernic.tools.transparent.domain.sources.UsbSerialPortDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule by lazy {
    module {
        single<UsbSerialPortDataSource> { UsbSerialPortDataSourceImpl(get() , androidContext()) }
        single<PhysicalSerialPortDataSource> { PhysicalSerialPortDataSourceImpl(androidContext()) }
        single { UsbSerialHelper(androidContext()) }
    }
}
