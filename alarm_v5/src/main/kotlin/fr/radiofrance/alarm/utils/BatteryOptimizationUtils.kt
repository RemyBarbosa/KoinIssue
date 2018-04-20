package fr.radiofrance.alarm.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import fr.radiofrance.alarm.R

object BatteryOptimizationUtils {

    @JvmStatic
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).let {
            return it.isIgnoringBatteryOptimizations(context.packageName)
        }
    }

    /**
     *
     * @param context (should be an activity context because use to build dialog)
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun showBatteryOptimizationDialog(context: Context, appName: String) {
        val cantLinkToDirectBatteryOptimizationDialog = ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED

        if (cantLinkToDirectBatteryOptimizationDialog) {
            showDialog(context,
                    context.getString(R.string.alarm_battery_optimization_list_dialog_message, appName),
                    R.string.alarm_battery_optimization_list_dialog_positive,
                    R.string.alarm_battery_optimization_list_dialog_negative,
                    Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            return
        }

        showDialog(context,
                context.getString(R.string.alarm_battery_optimization_direct_dialog_message, appName),
                R.string.alarm_battery_optimization_direct_dialog_positive,
                R.string.alarm_battery_optimization_direct_dialog_negative,
                Intent().setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + context.packageName)))
    }

    private fun showDialog(context: Context, message: String, @StringRes positiveTextId: Int, @StringRes negativeTextId: Int, intent: Intent) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(positiveTextId) { _, _ -> context.startActivity(intent) }
        builder.setNegativeButton(negativeTextId) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.show()
    }


}