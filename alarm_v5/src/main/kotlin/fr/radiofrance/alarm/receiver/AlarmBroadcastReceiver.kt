package fr.radiofrance.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
        Toast.makeText(context, "Action: ${intent.action}", Toast.LENGTH_LONG).show()
    }

}