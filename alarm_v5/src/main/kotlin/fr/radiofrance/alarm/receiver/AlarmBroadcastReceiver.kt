package fr.radiofrance.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import fr.radiofrance.alarm.schedule.AlarmIntentUtils
import fr.radiofrance.alarm.service.AlarmService
import java.text.SimpleDateFormat
import java.util.*


class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmBroadcastReceiver", "onReceive at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        context ?: return
        intent ?: return

        intent.action?.let {
            when (it) {
                AlarmIntentUtils.ALARM_CLOCK_ACTION -> return onAlarmClockReceive(context, intent)
                else -> return
            }
        }
    }

    private fun onAlarmClockReceive(context: Context, intent: Intent) {

        with(context.getSystemService(Context.POWER_SERVICE) as PowerManager) {
            newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE, "AlarmBroadcastReceiver")
        }.acquire(5000L)

        Intent(context, AlarmService::class.java)
                .apply {
                    putExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, intent.getLongExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, 0L))
                    putExtra(AlarmIntentUtils.ALARM_CLOCK_DATA_KEY, intent.getBundleExtra(AlarmIntentUtils.ALARM_CLOCK_DATA_KEY))
                }
                .let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(it)
                    } else {
                        context.startService(it)
                    }
                }
    }

}