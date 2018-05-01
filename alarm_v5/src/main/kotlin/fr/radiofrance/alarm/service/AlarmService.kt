package fr.radiofrance.alarm.service


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import fr.radiofrance.alarm.R
import fr.radiofrance.alarm.RfAlarmManager
import fr.radiofrance.alarm.activity.AlarmActivity
import fr.radiofrance.alarm.broadcast.AlarmIntentBuilder


class AlarmService : Service() {

    companion object {
        internal const val ALARM_SERVICE_START_ACTION = "fr.radiofrance.alarm.ALARM_SERVICE_START_ACTION"
        internal const val ALARM_SERVICE_USER_ON_SNOOZE_ACTION = "fr.radiofrance.alarm.ALARM_SERVICE_USER_ON_SNOOZE_ACTION"
        internal const val ALARM_SERVICE_USER_ON_STOP_ACTION = "fr.radiofrance.alarm.ALARM_SERVICE_USER_ON_STOP_ACTION"
        internal const val ALARM_SERVICE_USER_ON_CONTINUE_ACTION = "fr.radiofrance.alarm.ALARM_SERVICE_USER_ON_CONTINUE_ACTION"

        private const val NOTIFICATION_ID = 569754
        private const val NOTIFICATION_CHANNEL_ID = "rfalarm_notification_channel"
    }

    private val rfAlarmManager by lazy { RfAlarmManager(applicationContext) }

    private val ringtone by lazy { RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(this) }

    private val alarmCustomBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            intent?.action.takeIf { it == AlarmIntentBuilder.ALARM_CALLBACK_ON_RANG_CUSTOM_OK_ACTION }?.let {
                ringtone.stop()
                intent ?: return
                rfAlarmManager.onAlarmRangCustomOk(intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
                stopForeground(true)
            }
        }
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

        localBroadcastManager.registerReceiver(alarmCustomBroadcastReceiver, IntentFilter(AlarmIntentBuilder.ALARM_CALLBACK_ON_RANG_CUSTOM_OK_ACTION))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ALARM_SERVICE_START_ACTION -> { onStartAction(intent) }
            ALARM_SERVICE_USER_ON_CONTINUE_ACTION -> onUserContinueAction(intent)
            ALARM_SERVICE_USER_ON_SNOOZE_ACTION -> onUserSnoozeAction(intent)
            ALARM_SERVICE_USER_ON_STOP_ACTION -> onUserStopAction(intent)
        }
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        ringtone.stop()
        localBroadcastManager.unregisterReceiver(alarmCustomBroadcastReceiver)
        super.onDestroy()
    }

    private fun onStartAction(intent: Intent) {
        ringtone.play()
        launchActivity(intent)
        rfAlarmManager.onAlarmRang(intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
    }

    private fun onUserContinueAction(intent: Intent) {
        rfAlarmManager.onAlarmContinued(intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
        stopSelf()
    }

    private fun onUserSnoozeAction(intent: Intent) {
        rfAlarmManager.onAlarmSnoozed(intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
        stopSelf()
    }

    private fun onUserStopAction(intent: Intent) {
        rfAlarmManager.onAlarmStopped(intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
        stopSelf()
    }

    private fun launchActivity(intent: Intent) {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(applicationContext, AlarmActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, intent.getLongExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, 0L))
            putExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY, intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
        })
    }

}
