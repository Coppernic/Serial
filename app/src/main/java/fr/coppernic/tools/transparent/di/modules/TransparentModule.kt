package fr.coppernic.tools.transparent.di.modules

import dagger.Binds
import dagger.Module
import fr.coppernic.tools.transparent.transparent.TransparentFragment
import fr.coppernic.tools.transparent.transparent.TransparentPresenter
import fr.coppernic.tools.transparent.transparent.TransparentPresenterImpl
import fr.coppernic.tools.transparent.transparent.TransparentView

@Module
interface TransparentModule {
    @Binds
    fun bindTransparentView(view: TransparentFragment):TransparentView
    @Binds
    fun bindTransparentPresenter(presenter: TransparentPresenterImpl): TransparentPresenter
}