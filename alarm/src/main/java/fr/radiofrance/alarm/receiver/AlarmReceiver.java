package fr.radiofrance.alarm.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;

import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.manager.RfAlarmManager;

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
        try {
            RfAlarmManager.with(context).onAlarmDeprecatedBroadcastReceived(alarmId);
        } catch (RfAlarmException e) {
            e.printStackTrace();
        }
    }

}
