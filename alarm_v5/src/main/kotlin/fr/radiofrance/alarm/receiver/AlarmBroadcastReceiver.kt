package fr.radiofrance.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.radiofrance.alarm.activity.AlarmActivity
import fr.radiofrance.alarm.schedule.AlarmScheduler
import java.text.SimpleDateFormat
import java.util.*

class AlarmBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmBroadcastReceiver", "onReceive at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        context?: return
        intent?: return

        intent.action?.let {
            when(it) {
                AlarmScheduler.ALARM_CLOCK_ACTION -> return onAlarmClockReceive(context, intent)
                else -> return
            }
        }
    }

    private fun onAlarmClockReceive(context: Context, intent: Intent) {
        context.startActivity(AlarmActivity.newIntent(context,
                intent.getLongExtra(AlarmScheduler.ALARM_CLOCK_AT_TIME_KEY, 0L),
                intent.getBundleExtra(AlarmScheduler.ALARM_CLOCK_DATA_KEY)))
    }

}