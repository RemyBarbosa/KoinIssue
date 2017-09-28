package fr.radiofrance.alarm.util;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import fr.radiofrance.alarm.R;

public abstract class AlarmBatteryOptimizationUtils {

    /**
     *
     * @param context (should be an activity context because use to build dialog)
     */
    public static void showBatteryOptimizationDialogIfNeeded(final Context context, final String appName) {
        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.alarm_battery_optimization_dialog_title);
        builder.setMessage(context.getString(R.string.alarm_battery_optimization_dialog_message, appName));
        builder.setPositiveButton(R.string.alarm_battery_optimization_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                final Intent intent = new Intent();
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED) {
                    // Open the global settings screen
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                } else {
                    // Open the ignoring request dialog
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                }
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.alarm_battery_optimization_dialog_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

}
