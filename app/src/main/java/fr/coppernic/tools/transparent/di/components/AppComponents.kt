package fr.coppernic.tools.transparent.di.components

import dagger.Component
import fr.coppernic.tools.transparent.di.modules.ContextModule
import fr.coppernic.tools.transparent.di.modules.PresenterModule
import fr.coppernic.tools.transparent.home.HomeActivity
import javax.inject.Singleton


@Singleton
@Component(modules = [ContextModule::class, PresenterModule::class])
interface AppComponents {

    fun inject(homeActivity: HomeActivity)
}