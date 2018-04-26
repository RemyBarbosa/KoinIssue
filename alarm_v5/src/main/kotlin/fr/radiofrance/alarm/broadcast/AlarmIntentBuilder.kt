package fr.radiofrance.alarm.broadcast

import android.content.Context
import android.content.Intent
import android.os.Bundle

object AlarmIntentBuilder {

    const val ALARM_EXTRA_DATA_KEY = "fr.radiofrance.alarm.ALARM_EXTRA_DATA_KEY"
    const val ALARM_EXTRA_AT_TIME_KEY = "fr.radiofrance.alarm.ALARM_EXTRA_AT_TIME_KEY"

    const val ALARM_CLOCK_ACTION = "fr.radiofrance.alarm.ALARM_CLOCK_ACTION"
    const val ALARM_CLOCK_INFO_SHOW_ACTION = "fr.radiofrance.alarm.ALARM_CLOCK_INFO_SHOW_ACTION"

    const val ALARM_CALLBACK_ON_RANG_ACTION = "fr.radiofrance.alarm.ALARM_CALLBACK_ON_RANG_ACTION"


    fun buildClockAction(context: Context, timeInMillis: Long, data: Bundle) = buildIntent(
            context = context,
            action = ALARM_CLOCK_ACTION,
            timeInMillis = timeInMillis,
            data = data
    )

    fun buildClockInfoShowAction(context: Context, data: Bundle) = buildIntent(
            context = context,
            action = ALARM_CLOCK_INFO_SHOW_ACTION,
            data = data
    )

    fun buildCallbackOnRangAction(context: Context, data: Bundle) = buildIntent(
            context = context,
            action = ALARM_CALLBACK_ON_RANG_ACTION,
            data = data
    )

    private fun buildIntent(context: Context, action: String, timeInMillis: Long? = null, data: Bundle? = null) = Intent().apply {
        this.action = action
        this.`package` = context.packageName
        data?.let { putExtra(ALARM_EXTRA_DATA_KEY, it) }
        timeInMillis?.let { putExtra(ALARM_EXTRA_AT_TIME_KEY, it) }
    }

}