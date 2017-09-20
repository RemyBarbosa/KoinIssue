package fr.radiofrance.alarm.receiver;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;

import fr.radiofrance.alarm.datastore.ConfigurationDatastore;
import fr.radiofrance.alarm.util.AlarmIntentUtils;

@Deprecated
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Deprecated
    public static final String KEY_ALARM = AlarmReceiver.class.getSimpleName() + "KEY_ALARM";
    @Deprecated
    public static final String KEY_SNOOZE = AlarmReceiver.class.getSimpleName() + "KEY_SNOOZE";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        Log.v("AlarmReceiver", "onReceive action (deprecated): " + action);
        if (context == null || TextUtils.isEmpty(action)) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Do nothing, new boot receiver make the job
        } else if (action.startsWith(KEY_ALARM) || action.startsWith(KEY_SNOOZE)) {
            // Do recovery on old version alarm receive
            recovery(context, action);
        }
    }

    private void recovery(@NonNull final Context context, @NonNull final String action) {
        String alarmId = null;
        if (action.startsWith(KEY_ALARM)) {
            alarmId = action.replace(KEY_ALARM, "");
        } else if (action.startsWith(KEY_SNOOZE)) {
            alarmId = action.replace(KEY_SNOOZE, "");
        }

        // TODO recovery : getAlarmDefaultLaunchIntent from RfAlarmManager
        final Intent newLaunchIntent = new ConfigurationDatastore(context).getAlarmDefaultLaunchIntent(null);
        if (newLaunchIntent == null) {
            return;
        }
        newLaunchIntent.putExtra(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID, alarmId);

        if (newLaunchIntent.getComponent() != null) {
            try {
                final Class<?> act = Class.forName(newLaunchIntent.getComponent().getClassName());
                if (Activity.class.isAssignableFrom(act)) {
                    newLaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(newLaunchIntent);
                    return;
                }
                if (BroadcastReceiver.class.isAssignableFrom(act)) {
                    context.sendBroadcast(newLaunchIntent);
                    return;
                }
                if (Service.class.isAssignableFrom(act)) {
                    context.startService(newLaunchIntent);
                    return;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
