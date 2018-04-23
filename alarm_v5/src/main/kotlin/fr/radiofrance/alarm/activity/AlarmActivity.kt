package fr.radiofrance.alarm.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
            intent.putExtra(INTENT_EXTRA_AT_TIME_MILLIS_KEY, atTimeMillis)
            intent.putExtra(INTENT_EXTRA_DATA_KEY, data)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        main_hour_textview.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date())
        main_expected_hour_textview.text = "Expected: ${SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(intent.getLongExtra(INTENT_EXTRA_AT_TIME_MILLIS_KEY, 0L)))}"
    }
}
