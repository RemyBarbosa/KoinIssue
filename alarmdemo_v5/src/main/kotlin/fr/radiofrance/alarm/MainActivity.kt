package fr.radiofrance.alarm

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import fr.radiofrance.alarm.utils.BatteryOptimizationUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val alarmManager by lazy {
        RfAlarmManager(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_version_textview.text = "OS version :  ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})"
        main_ignore_battery_optim_textview.text = "Ignore battery optimization :  ${BatteryOptimizationUtils.isIgnoringBatteryOptimizations(applicationContext)}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            main_ignore_battery_optim_button.visibility = View.VISIBLE
            main_ignore_battery_optim_button.setOnClickListener {
                BatteryOptimizationUtils.showBatteryOptimizationDialog(this, getString(R.string.app_name))
            }
        }

        main_program_alarm_in_ten_seconds_button.setOnClickListener {
            alarmManager.tempTestScheduleAt(Calendar.getInstance().apply {
                add(Calendar.SECOND, 10)
            }.timeInMillis)
        }

        main_program_alarm_in_one_minute_button.setOnClickListener {
            alarmManager.tempTestScheduleAt(Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
            }.timeInMillis)
        }

        main_program_alarm_in_ten_minutes_button.setOnClickListener {
            alarmManager.tempTestScheduleAt(Calendar.getInstance().apply {
                add(Calendar.MINUTE, 10)
            }.timeInMillis)
        }

        main_program_alarm_in_two_hour_button.setOnClickListener {
            alarmManager.tempTestScheduleAt(Calendar.getInstance().apply {
                add(Calendar.HOUR, 2)
            }.timeInMillis)
        }

    }

    override fun onResume() {
        super.onResume()
        main_ignore_battery_optim_textview.text = "Ignore battery optimization :  ${BatteryOptimizationUtils.isIgnoringBatteryOptimizations(applicationContext)}"
    }
}
