package fr.radiofrance.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.radiofrance.alarm.activity.AlarmActivity
import fr.radiofrance.alarm.schedule.AlarmScheduler

class AlarmBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
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