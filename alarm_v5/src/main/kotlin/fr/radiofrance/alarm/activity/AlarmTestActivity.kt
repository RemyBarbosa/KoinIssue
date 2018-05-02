package fr.radiofrance.alarm.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import android.view.WindowManager
import fr.radiofrance.alarm.R
import fr.radiofrance.alarm.broadcast.AlarmIntentBuilder
import fr.radiofrance.alarm.service.AlarmService
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.SimpleDateFormat
import java.util.*


class AlarmTestActivity : AppCompatActivity() {

    companion object {
        private const val WAKE_LOCK_TIMEOUT_MILLIS = DateUtils.MINUTE_IN_MILLIS
    }

    private val wakeLock by lazy {
        with(getSystemService(Context.POWER_SERVICE) as PowerManager) {
            newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE, "AlarmTestActivity")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AlarmTestActivity", "onCreate at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))

        super.onCreate(savedInstanceState)

        wakeLock.acquire(WAKE_LOCK_TIMEOUT_MILLIS)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        /*
        // fill status bar with a theme dark color on post-Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Theme.get(App.getState().settings().theme()).primaryDarkColor
        }
        */

        setContentView(R.layout.activity_alarm_test)

        main_expected_hour_textview.text = "Expected: ${SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(intent.getLongExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, 0L)))}"

        intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY).let { data ->
            main_stop_button.setOnClickListener {
                applicationContext.startService(Intent(applicationContext, AlarmService::class.java).apply {
                    action = AlarmService.ALARM_SERVICE_USER_ON_STOP_ACTION
                    putExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY, data)
                })
                closeScreen()
            }
            main_snooze_button.setOnClickListener {
                applicationContext.startService(Intent(applicationContext, AlarmService::class.java).apply {
                    action = AlarmService.ALARM_SERVICE_USER_ON_SNOOZE_ACTION
                    putExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY, data)
                })
                closeScreen()
            }
            main_continue_button.setOnClickListener {
                applicationContext.startService(Intent(applicationContext, AlarmService::class.java).apply {
                    action = AlarmService.ALARM_SERVICE_USER_ON_CONTINUE_ACTION
                    putExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY, data)
                })
                closeScreen()
            }
        }
    }

    override fun onResume() {
        Log.d("AlarmTestActivity", "onResume at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onResume()

        if (!main_hour_textview.text.isNullOrEmpty()) {
            return
        }

        main_hour_textview.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())

        if ((System.currentTimeMillis() - intent.getLongExtra(AlarmIntentBuilder.ALARM_EXTRA_AT_TIME_KEY, 0L)) > 30 * DateUtils.SECOND_IN_MILLIS) {
            main_constraintlayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.test_failed_background_color))
        } else {
            main_constraintlayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.test_succeed_background_color))
        }
    }

    override fun onUserLeaveHint() {
        closeScreen()
        super.onUserLeaveHint()
    }

    override fun onBackPressed() {
        // Do nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun closeScreen() {
        finish()
    }
}
