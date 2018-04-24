package fr.radiofrance.alarm.activity

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import fr.radiofrance.alarm.R
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.SimpleDateFormat
import java.util.*





class AlarmActivity : AppCompatActivity() {

    companion object {

        private const val INTENT_EXTRA_AT_TIME_MILLIS_KEY = "extra_at_time_key"
        private const val INTENT_EXTRA_DATA_KEY = "extra_data_key"

        fun newIntent(context: Context, atTimeMillis: Long, data: Bundle): Intent {
            val intent = Intent(context, AlarmActivity::class.java)
            intent.flags = Intent.FLAG_FROM_BACKGROUND or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            intent.putExtra(INTENT_EXTRA_AT_TIME_MILLIS_KEY, atTimeMillis)
            intent.putExtra(INTENT_EXTRA_DATA_KEY, data)
            return intent
        }
    }

    private val ringtone by lazy { RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AlarmActivity", "onCreate at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))

        // Showing on lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        super.onCreate(savedInstanceState)

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "Alarm")
        wakeLock.acquire(DateUtils.HOUR_IN_MILLIS)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Hide Navigation bar
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }

        setContentView(R.layout.activity_alarm)


        main_expected_hour_textview.text = "Expected: ${SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(intent.getLongExtra(INTENT_EXTRA_AT_TIME_MILLIS_KEY, 0L)))}"

        ringtone.play()
    }

    override fun onResume() {
        Log.d("AlarmActivity", "onResume at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onResume()
        main_hour_textview.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        ringtone.play()
    }

    override fun onStop() {
        Log.d("AlarmActivity", "onStop at : " + SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date()))
        super.onStop()
        ringtone.stop()
    }
}
