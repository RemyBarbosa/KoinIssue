package fr.radiofrance.alarm

import fr.radiofrance.alarm.di.offlineAlarmApp
import org.koin.android.ext.android.startKoin

/**
 * Main Application
 */
class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        // start Koin context
        startKoin(this, offlineAlarmApp)
    }
}
