package fr.coppernic.tools.transparent.di.modules

import fr.coppernic.tools.transparent.transparent.SerialPortViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val viewModelModule = module {
    viewModel { SerialPortViewModel(get(), get()) }
}
