package fr.radiofrance.alarm.broadcast

import android.content.Context
import android.content.Intent
import android.os.Bundle

object AlarmIntentBuilder {

    const val ALARM_CLOCK_ACTION = "fr.radiofrance.alarm.ALARM_CLOCK_ACTION"
    const val ALARM_CLOCK_INFO_SHOW_ACTION = "fr.radiofrance.alarm.ALARM_CLOCK_INFO_SHOW_ACTION"

    const val ALARM_EXTRA_DATA_KEY = "fr.radiofrance.alarm.ALARM_EXTRA_DATA_KEY"
    const val ALARM_EXTRA_AT_TIME_KEY = "fr.radiofrance.alarm.ALARM_EXTRA_AT_TIME_KEY"

    fun buildClockAction(context: Context, timeInMillis: Long, data: Bundle) = Intent().apply {
        action = ALARM_CLOCK_ACTION
        `package` = context.packageName
        putExtra(ALARM_EXTRA_AT_TIME_KEY, timeInMillis)
        putExtra(ALARM_EXTRA_DATA_KEY, data)
    }

    fun buildClockInfoShowAction(context: Context, data: Bundle) = Intent().apply {
        action = ALARM_CLOCK_INFO_SHOW_ACTION
        `package` = context.packageName
        putExtra(ALARM_EXTRA_DATA_KEY, data)
    }

}