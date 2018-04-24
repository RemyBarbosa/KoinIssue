package fr.radiofrance.alarm.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(atTimeInMillis, getAlarmClockInfoShowPendingIntent(data)), pendingIntent)
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,  atTimeInMillis, pendingIntent)
            }*/
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, atTimeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, pendingIntent)
        }
    }

    private fun getAlarmClockPendingIntent(atTimeInMillis: Long, data: Bundle) =
            PendingIntent.getBroadcast(context,
                    ALARM_CLOCK_RESQUEST_CODE,
                    Intent().apply {
                        action = AlarmIntentUtils.ALARM_CLOCK_ACTION
                        `package` = context.packageName
                        putExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, atTimeInMillis)
                        putExtra(AlarmIntentUtils.ALARM_CLOCK_DATA_KEY, data)
                    },
                    PendingIntent.FLAG_CANCEL_CURRENT)
    /*
    private fun getAlarmClockPendingIntent(atTimeInMillis: Long, data: Bundle) =
            PendingIntent.getActivity(context,
                    ALARM_CLOCK_RESQUEST_CODE,
                    AlarmActivity.newIntent(context, atTimeInMillis, data),
                    PendingIntent.FLAG_CANCEL_CURRENT)
                    */

    private fun getAlarmClockInfoShowPendingIntent(data: Bundle) =
            PendingIntent.getBroadcast(context,
                    ALARM_CLOCK_INFO_SHOW_RESQUEST_CODE,
                    Intent().apply {
                        action = AlarmIntentUtils.ALARM_CLOCK_INFO_SHOW_ACTION
                        `package` = context.packageName
                        putExtra(AlarmIntentUtils.ALARM_CLOCK_INFO_SHOW_DATA_KEY, data)
                    },
                    PendingIntent.FLAG_ONE_SHOT)

}