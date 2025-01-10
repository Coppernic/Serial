package fr.coppernic.tools.transparent

import android.app.Application
import fr.coppernic.tools.transparent.di.components.AppComponents
import fr.coppernic.tools.transparent.di.components.DaggerAppComponents
import fr.coppernic.tools.transparent.di.modules.ContextModule
import fr.coppernic.tools.transparent.di.modules.androidModule
import fr.coppernic.tools.transparent.di.modules.dataModule
import fr.coppernic.tools.transparent.di.modules.repoModule
import fr.coppernic.tools.transparent.di.modules.settingsKoinModule
import fr.coppernic.tools.transparent.di.modules.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    companion object {
        @JvmStatic
        lateinit var appComponents: AppComponents
    }

    override fun onCreate() {
        super.onCreate()
        setupDi()
        setupDiKoin()
        setupLog()
    }

    private fun setupDi() {
        appComponents = DaggerAppComponents.builder()
                .contextModule(ContextModule(this))
                .build()
    }

    private fun setupDiKoin() {
        startKoin {
            androidContext(this@App)
            modules(
                repoModule,
                dataModule,
                viewModelModule,
                settingsKoinModule,
                androidModule,
            )
        }
    }

    private fun setupLog() {
        Timber.plant(Timber.DebugTree())
    }
}