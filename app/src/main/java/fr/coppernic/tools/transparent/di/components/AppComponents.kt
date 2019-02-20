package fr.coppernic.tools.transparent.di.components

import dagger.Component
import fr.coppernic.tools.transparent.di.modules.ContextModule
import fr.coppernic.tools.transparent.di.modules.SettingsModule
import fr.coppernic.tools.transparent.di.modules.TerminalModule
import fr.coppernic.tools.transparent.di.modules.TransparentModule
import fr.coppernic.tools.transparent.home.HomeActivity
import javax.inject.Singleton


@Singleton
@Component(modules = [ContextModule::class, TransparentModule::class, TerminalModule::class, SettingsModule::class])
interface AppComponents {
    fun inject(homeActivity: HomeActivity)
}