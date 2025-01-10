package fr.coppernic.tools.transparent.di.modules

import fr.coppernic.tools.transparent.data.repositories.SerialPortRepoImpl
import fr.coppernic.tools.transparent.domain.repositories.SerialPortRepo
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import fr.coppernic.tools.transparent.settings.SettingsInteractorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsKoinModule by lazy {
    module {
        single<SettingsInteractor> { SettingsInteractorImpl(androidContext(), get()) }
    }
}
