package fr.radiofrance.alarm

import android.content.Context
import android.os.Bundle
import fr.radiofrance.alarm.schedule.AlarmScheduler

class RfAlarmManager(context: Context) {

    private val scheduler = AlarmScheduler(context)

    fun tempTestScheduleAt(scheduleTimeMillis: Long) {
        scheduler.scheduleAt(scheduleTimeMillis, Bundle().apply {
            putString("ALARM_TEST_KEY", "Alarm test key content")
        })
    }

}