package fr.radiofrance.alarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import fr.radiofrance.alarm.broadcast.AlarmIntentBuilder
import fr.radiofrance.alarm.schedule.AlarmScheduler
import fr.radiofrance.alarm.service.AlarmService

class RfAlarmManager(val context: Context) {

    private val scheduler = AlarmScheduler(context)

    fun tempTestScheduleAt(scheduleTimeMillis: Long) {
        scheduler.scheduleAt(scheduleTimeMillis, Bundle().apply {
            putString("ALARM_TEST_KEY", "Alarm test key content")
        })
    }

    /**
     * Call when alarm activity is displaid
     */
    internal fun onAlarmRang(data: Bundle) {
        // Send broadcast for app receiver playing radio
        context.sendBroadcast(AlarmIntentBuilder.buildCallbackOnRangAction(context, data))

        // TODO program next alarm

        // TODO tracking
    }

    /**
     * Call when custom alarm from app has started to play
     */
    internal fun onAlarmRangCustomOk(data: Bundle) {
        // When custom alarm from app is ok, we can safely stop foreground service to remove notification
        context.startService(Intent(context, AlarmService::class.java).apply {
            action = AlarmIntentBuilder.ALARM_SERVICE_STOP_FOREGROUND_ACTION
        })

        // TODO tracking
    }

    /**
     * Call when alarm activity stop button is click
     */
    internal fun onAlarmStopped(data: Bundle) {
        // Send broadcast for app receiver playing radio
        context.sendBroadcast(AlarmIntentBuilder.buildCallbackOnStopAction(context, data))

        // TODO tracking
    }

    /**
     * Call when alarm activity snooze button is click
     */
    internal fun onAlarmSnoozed(data: Bundle) {
        // Send broadcast for app receiver playing radio
        context.sendBroadcast(AlarmIntentBuilder.buildCallbackOnStopAction(context, data))

        // TODO program snooze

        // TODO tracking
    }

    /**
     * Call when alarm activity continue button is click
     */
    internal fun onAlarmContinued(data: Bundle) {

        // TODO tracking

    }
}