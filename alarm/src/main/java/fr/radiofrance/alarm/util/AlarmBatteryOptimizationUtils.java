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
import android.support.annotation.StringRes;
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
        if (powerManager == null || powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
            return;
        }

        final boolean cantLinkToDirectBatteryOptimizationDialog = ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED;

        if (cantLinkToDirectBatteryOptimizationDialog) {
            showDialog(context,
                    context.getString(R.string.alarm_battery_optimization_list_dialog_message, appName),
                    R.string.alarm_battery_optimization_list_dialog_positive,
                    R.string.alarm_battery_optimization_list_dialog_negative,
                    new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            return;
        }

        showDialog(context,
                context.getString(R.string.alarm_battery_optimization_direct_dialog_message, appName),
                R.string.alarm_battery_optimization_direct_dialog_positive,
                R.string.alarm_battery_optimization_direct_dialog_negative,
                new Intent().setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + context.getPackageName())));
    }

    private static void showDialog(final Context context, final String message, @StringRes final int positiveTextId, @StringRes final int negativeTextId, final Intent intent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(positiveTextId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(negativeTextId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

}
