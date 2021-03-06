package fr.radiofrance.alarm

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val alarmManager by lazy {
        RfAlarmManager(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_version_textview.text = "OS version :  ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})"

        main_program_alarm_in_ten_seconds_button.setOnClickListener {
            programAlarmAtTime(Calendar.getInstance().apply {
                add(Calendar.SECOND, 10)
            }.timeInMillis)
        }

        main_program_alarm_in_one_minute_button.setOnClickListener {
            programAlarmAtTime(Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
            }.timeInMillis)
        }

        main_program_alarm_in_ten_minutes_button.setOnClickListener {
            programAlarmAtTime(Calendar.getInstance().apply {
                add(Calendar.MINUTE, 10)
            }.timeInMillis)
        }

        main_program_alarm_in_two_hour_button.setOnClickListener {
            programAlarmAtTime(Calendar.getInstance().apply {
                add(Calendar.HOUR, 2)
            }.timeInMillis)
        }

        main_program_custom_button.setOnClickListener {
            val input = Calendar.getInstance()
            TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            if (timeInMillis < System.currentTimeMillis()) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.let { programAlarmAtTime(it.timeInMillis) }

                    },
                    input.get(Calendar.HOUR_OF_DAY), input.get(Calendar.MINUTE), false
            ).show()
        }

    }

    private fun programAlarmAtTime(timeMillis: Long) {
        alarmManager.tempTestScheduleAt(timeMillis)
        Snackbar.make(main_program_custom_button, "Alarm set at: ${SimpleDateFormat("h:mm:ss a", Locale.US).format(timeMillis)}", Snackbar.LENGTH_LONG).show()
    }
}
