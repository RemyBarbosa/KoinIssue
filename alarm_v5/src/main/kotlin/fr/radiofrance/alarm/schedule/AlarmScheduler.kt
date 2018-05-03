package fr.radiofrance.alarm.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import fr.radiofrance.alarm.broadcast.AlarmIntentBuilder

class AlarmScheduler(private val context: Context) {

    companion object {
        const val ALARM_CLOCK_RESQUEST_CODE = 64678
        const val ALARM_CLOCK_INFO_SHOW_RESQUEST_CODE = 64654
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAt(timeMillis: Long, data: Bundle) {
        scheduleInAlarmSystem(
                getAlarmClockPendingIntent(timeMillis, data),
                timeMillis,
                data
        )
    }

    private fun scheduleInAlarmSystem(pendingIntent: PendingIntent, atTimeInMillis: Long, data: Bundle) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(atTimeInMillis, getAlarmClockInfoShowPendingIntent(data)), pendingIntent)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ->
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, atTimeInMillis, pendingIntent)
            else ->
                alarmManager.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, pendingIntent)
        }
    }

    private fun getAlarmClockPendingIntent(atTimeInMillis: Long, data: Bundle) =
            PendingIntent.getBroadcast(context,
                    ALARM_CLOCK_RESQUEST_CODE,
                    AlarmIntentBuilder.buildClockAction(context, atTimeInMillis, data),
                    PendingIntent.FLAG_CANCEL_CURRENT)

    private fun getAlarmClockInfoShowPendingIntent(data: Bundle) =
            PendingIntent.getBroadcast(context,
                    ALARM_CLOCK_INFO_SHOW_RESQUEST_CODE,
                    AlarmIntentBuilder.buildClockInfoShowAction(context, data),
                    PendingIntent.FLAG_ONE_SHOT)

}