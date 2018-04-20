package fr.radiofrance.alarm

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import fr.radiofrance.alarm.utils.BatteryOptimizationUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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


        //RfAlarmManager().message

    }

    override fun onResume() {
        super.onResume()
        main_ignore_battery_optim_textview.text = "Ignore battery optimization :  ${BatteryOptimizationUtils.isIgnoringBatteryOptimizations(applicationContext)}"
    }
}
