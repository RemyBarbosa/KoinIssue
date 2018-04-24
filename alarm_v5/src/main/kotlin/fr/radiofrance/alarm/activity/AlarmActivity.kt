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
import fr.radiofrance.alarm.schedule.AlarmIntentUtils
import fr.radiofrance.alarm.service.AlarmService
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.SimpleDateFormat
import java.util.*


class AlarmActivity : AppCompatActivity() {


    //private val ringtone by lazy { RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }


    private val wakeLock by lazy {
        with(getSystemService(Context.POWER_SERVICE) as PowerManager) {
            newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE, "AlarmActivity")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AlarmActivity", "onCreate at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))

        super.onCreate(savedInstanceState)

        wakeLock.acquire(60000L)
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


        setContentView(R.layout.activity_alarm)


        main_expected_hour_textview.text = "Expected: ${SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(intent.getLongExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, 0L)))}"

        //ringtone.play()
    }

    override fun onResume() {
        Log.d("AlarmActivity", "onResume at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onResume()
        main_hour_textview.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        //ringtone.play()

        if ((System.currentTimeMillis() - intent.getLongExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, 0L)) > 30 * DateUtils.SECOND_IN_MILLIS) {
            main_constraintlayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.test_failed_background_color))
        } else {
            main_constraintlayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.test_succeed_background_color))
        }
    }

    override fun onStop() {
        Log.d("AlarmActivity", "onStop at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onStop()
        //ringtone.stop()
    }

    override fun onUserLeaveHint() {
        stopAlarm()
        super.onUserLeaveHint()
    }

    override fun onBackPressed() {
        stopAlarm()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun stopAlarm() {
        applicationContext.stopService(Intent(applicationContext, AlarmService::class.java))
        finish()
    }
}
