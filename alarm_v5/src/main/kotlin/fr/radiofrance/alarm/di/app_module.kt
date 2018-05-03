package fr.radiofrance.alarm.di

import android.arch.persistence.room.Room
import fr.radiofrance.alarm.data.datasource.room.AlarmDatabase
import fr.radiofrance.alarm.data.repository.AlarmRepository
import fr.radiofrance.alarm.data.repository.AlarmRepositoryImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

/**
 * App Components
 */
val alarmAppModule = applicationContext {

    // Room Database
    bean {
        Room.databaseBuilder(androidApplication(), AlarmDatabase::class.java, "alarm-db")
                .build()
    }
    // Expose WeatherDAO directly
    bean { get<AlarmDatabase>().alarmDAO() }
}

val offlineAlarmApp = listOf(alarmAppModule)