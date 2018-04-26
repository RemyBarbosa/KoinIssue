package fr.radiofrance.alarm.service


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import fr.radiofrance.alarm.R
import fr.radiofrance.alarm.activity.AlarmActivity
import fr.radiofrance.alarm.broadcast.AlarmIntentBuilder


class AlarmService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 569754
        private const val NOTIFICATION_CHANNEL_ID = "rfalarm_notification_channel"
    }

    @SuppressLint("NewApi")
    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(
                        NotificationChannel(NOTIFICATION_CHANNEL_ID,
                                getString(R.string.alarm_notif_label_channel),
                                NotificationManager.IMPORTANCE_LOW)
                                .apply {
                                    enableVibration(false)
                                    enableLights(false)
                                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                                }
                )
            }
        }

        startForeground(NOTIFICATION_ID,
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm_notification)
                        // TODO set a good content text
                        .setContentText(getString(R.string.alarm_notif_label))
                        .build()
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(applicationContext, AlarmActivity::class.java)
            setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, intent.getLongExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, 0L))
            putExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY, intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
        })

        sendBroadcast(AlarmIntentBuilder.buildCallbackOnRangAction(applicationContext, intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY)))

        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
