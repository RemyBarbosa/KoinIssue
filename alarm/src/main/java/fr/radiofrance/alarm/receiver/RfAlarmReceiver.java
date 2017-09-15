package fr.radiofrance.alarm.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import fr.radiofrance.alarm.manager.RfAlarmManager;


public class RfAlarmReceiver extends BroadcastReceiver {

    // TODO
    //
    // Boot : Add recovery module when read object from preferences
    //
    // Add notifications


    public static void enable(final Context context) {
        setReceiverEnabledSetting(context, true);
    }

    public static void disable(final Context context) {
        setReceiverEnabledSetting(context, false);
    }

    private static void setReceiverEnabledSetting(final Context context, final boolean enable) {
        final ComponentName componentName = new ComponentName(context, RfAlarmReceiver.class);
        final PackageManager packageManager = context.getPackageManager();

        packageManager.setComponentEnabledSetting(componentName,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        RfAlarmManager.with(context).onDeviceReboot();
    }
}
