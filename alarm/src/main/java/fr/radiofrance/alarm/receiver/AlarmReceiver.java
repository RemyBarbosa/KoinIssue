package fr.radiofrance.alarm.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;

import fr.radiofrance.alarm.exception.RfAlarmDefaultLaunchIntentNotFoundException;
import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.manager.RfAlarmManager;

/**
 * @deprecated
 * This class should not be move or rename.
 * Because broadcasts previously set in system with previous version of the lib know only the package and the class names.
 */
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
        callDeprecatedCallback(RfAlarmManager.with(context), alarmId);
    }

    private void callDeprecatedCallback(final RfAlarmManager rfAlarmManager, final String alarmId) {
        if (rfAlarmManager == null) {
            return;
        }
        try {
            Log.v("AlarmReceiver", "onAlarmDeprecatedBroadcastReceived: " + alarmId);
            rfAlarmManager.onAlarmDeprecatedBroadcastReceived(alarmId);

        } catch (RfAlarmDefaultLaunchIntentNotFoundException e) {
            Log.w("AlarmReceiver", "AlarmDefaultLaunchIntentNotFoundException, will retry after 10sec.");
            // In the case of an update of the app and user hadn't launch the app before receiving old deprecated broadcast
            // the configuration can be not already set, so we do one retry after 10sec
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.v("AlarmReceiver", "onAlarmDeprecatedBroadcastReceived (retry): " + alarmId);
                        rfAlarmManager.onAlarmDeprecatedBroadcastReceived(alarmId);

                    } catch (RfAlarmException e) {
                        e.printStackTrace();
                    }
                }
            }, 10000L);
        } catch (RfAlarmException e) {
            e.printStackTrace();
        }
    }

}
