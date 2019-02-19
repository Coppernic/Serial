package fr.coppernic.tools.transparent.di.modules

import dagger.Binds
import dagger.Module
import fr.coppernic.tools.transparent.home.HomePresenter
import fr.coppernic.tools.transparent.home.HomePresenterImpl

@Module
interface PresenterModule {
    @Binds
    fun bindHomePresenter(presenter:HomePresenterImpl): HomePresenter
}