package fr.radiofrance.di

import android.arch.persistence.room.Room
import fr.radiofrance.alarm.data.datasource.room.AlarmDatabase
import org.koin.dsl.module.applicationContext

/**
 * In-Memory Room Database definition
 */
val roomTestModule = applicationContext {
    bean {
        Room.inMemoryDatabaseBuilder(get(), AlarmDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    bean { get<AlarmDatabase>().alarmDAO() }
}