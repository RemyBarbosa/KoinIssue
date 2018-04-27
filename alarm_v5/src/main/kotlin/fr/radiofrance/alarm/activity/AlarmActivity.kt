package fr.radiofrance.alarm.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import android.view.WindowManager
import fr.radiofrance.alarm.R
import fr.radiofrance.alarm.RfAlarmManager
import fr.radiofrance.alarm.broadcast.AlarmIntentBuilder
import fr.radiofrance.alarm.service.AlarmService
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.SimpleDateFormat
import java.util.*


class AlarmActivity : AppCompatActivity() {

    private val rfAlarmManager by lazy { RfAlarmManager(applicationContext) }

    private val ringtone by lazy { RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }

    private val wakeLock by lazy {
        with(getSystemService(Context.POWER_SERVICE) as PowerManager) {
            newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE, "AlarmActivity")
        }
    }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(this) }

    private val alarmCustomBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            intent?.action.takeIf { it == AlarmIntentBuilder.ALARM_CALLBACK_ON_RANG_CUSTOM_OK_ACTION }?.let {
                ringtone.stop()
                intent ?: return
                rfAlarmManager.onAlarmRangCustomOk(intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AlarmActivity", "onCreate at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))

        super.onCreate(savedInstanceState)

        wakeLock.acquire(60000L)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        localBroadcastManager.registerReceiver(alarmCustomBroadcastReceiver, IntentFilter(AlarmIntentBuilder.ALARM_CALLBACK_ON_RANG_CUSTOM_OK_ACTION))

        /*
        // fill status bar with a theme dark color on post-Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Theme.get(App.getState().settings().theme()).primaryDarkColor
        }
        */

        setContentView(R.layout.activity_alarm)

        main_expected_hour_textview.text = "Expected: ${SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(intent.getLongExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, 0L)))}"

        intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY).let { data ->
            rfAlarmManager.onAlarmRang(data)
            main_stop_button.setOnClickListener {
                rfAlarmManager.onAlarmStopped(data)
                closeScreen()
            }
            main_snooze_button.setOnClickListener {
                rfAlarmManager.onAlarmSnoozed(data)
                closeScreen()
            }
            main_continue_button.setOnClickListener {
                rfAlarmManager.onAlarmContinued(data)
                closeScreen()
            }
        }
    }

    override fun onResume() {
        Log.d("AlarmActivity", "onResume at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onResume()
        main_hour_textview.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        ringtone.play()

        if ((System.currentTimeMillis() - intent.getLongExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, 0L)) > 30 * DateUtils.SECOND_IN_MILLIS) {
            main_constraintlayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.test_failed_background_color))
        } else {
            main_constraintlayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.test_succeed_background_color))
        }
    }

    override fun onStop() {
        Log.d("AlarmActivity", "onStop at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onStop()
        ringtone.stop()
    }

    override fun onUserLeaveHint() {
        closeScreen()
        super.onUserLeaveHint()
    }

    override fun onBackPressed() {
        closeScreen()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(alarmCustomBroadcastReceiver)
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun closeScreen() {
        applicationContext.stopService(Intent(applicationContext, AlarmService::class.java))
        finish()
    }
}
