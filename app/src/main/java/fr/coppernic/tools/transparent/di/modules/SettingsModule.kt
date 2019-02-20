package fr.coppernic.tools.transparent.di.modules

import dagger.Binds
import dagger.Module
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import fr.coppernic.tools.transparent.settings.SettingsInteractorAndroid

@Module
interface SettingsModule {
    @Binds
    fun bindSettings(settings:SettingsInteractorAndroid):SettingsInteractor
}