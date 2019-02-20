package fr.coppernic.tools.transparent.di.modules

import dagger.Binds
import dagger.Module
import fr.coppernic.tools.transparent.terminal.TerminalFragment
import fr.coppernic.tools.transparent.terminal.TerminalPresenter
import fr.coppernic.tools.transparent.terminal.TerminalPresenterImpl
import fr.coppernic.tools.transparent.terminal.TerminalView

@Module
interface TerminalModule {

    @Binds
    fun bindTerminalView(view: TerminalFragment):TerminalView

    @Binds
    fun bindTerminalPresenter(presenter:TerminalPresenterImpl): TerminalPresenter
}