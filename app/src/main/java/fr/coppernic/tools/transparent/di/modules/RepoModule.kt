package fr.coppernic.tools.transparent.di.modules

import fr.coppernic.tools.transparent.data.repositories.SerialPortRepoImpl
import fr.coppernic.tools.transparent.domain.repositories.SerialPortRepo
import org.koin.dsl.module

val repoModule by lazy {
    module {
        single<SerialPortRepo> { SerialPortRepoImpl(get(), get()) }
    }
}
