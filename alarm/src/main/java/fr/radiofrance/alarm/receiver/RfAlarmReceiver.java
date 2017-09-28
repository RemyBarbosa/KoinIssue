package fr.radiofrance.alarm.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarm.notification.AlarmNotificationManager;
import fr.radiofrance.alarm.util.AlarmIntentUtils;


public class RfAlarmReceiver extends BroadcastReceiver {

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
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            RfAlarmManager.with(context).onDeviceReboot();
            return;
        }
        if (AlarmIntentUtils.buildActionWithPackageName(context, AlarmNotificationManager.ACTION_ALARM_NOTIFICATION_SHOW_UPCOMING)
                .equals(action)) {
            onAlarmNotificationShowUpcoming(context, intent);
            return;
        }
        if (AlarmIntentUtils.buildActionWithPackageName(context, AlarmNotificationManager.ACTION_ALARM_NOTIFICATION_CANCEL)
                .equals(action)) {
            onAlarmNotificationCancel(context, intent);
            return;
        }
    }

    private void onAlarmNotificationShowUpcoming(final Context context, final Intent intent) {
        try {
            RfAlarmManager.with(context)
                    .onAlarmNotificationShowBroadcastReceived(intent.getStringExtra(AlarmNotificationManager.EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY),
                            intent.getLongExtra(AlarmNotificationManager.EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY, -1L),
                            intent.getBooleanExtra(AlarmNotificationManager.EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY, false));
        } catch (RfAlarmException e) {
            e.printStackTrace();
        }
    }

    private void onAlarmNotificationCancel(final Context context, final Intent intent) {
        try {
            RfAlarmManager.with(context)
                    .onAlarmNotificationCancelBroadcastReceived(intent.getStringExtra(AlarmNotificationManager.EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY),
                            intent.getLongExtra(AlarmNotificationManager.EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY, -1L),
                            intent.getBooleanExtra(AlarmNotificationManager.EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY, false));
        } catch (RfAlarmException e) {
            e.printStackTrace();
        }
    }

}
