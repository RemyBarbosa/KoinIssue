package fr.radiofrance.alarm.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import java.util.List;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String KEY_ALARM = AlarmReceiver.class.getSimpleName() + "KEY_ALARM";
    public static final String KEY_SNOOZE = AlarmReceiver.class.getSimpleName() + "KEY_SNOOZE";
    public static final int TYPE_ALARM = 0;
    public static final int TYPE_SNOOZE = 1;

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            List<Alarm> alarms = AlarmManager.getAllAlarms(context);
            for (Alarm alarm : alarms) {
                if (alarm.isActivated()) {
                    AlarmManager.updateAlarm(context, alarm);
                }
            }
        } else if (action.startsWith(KEY_ALARM) || action.startsWith(KEY_SNOOZE)) {
            triggerAlarmIntent(context, action);
        }
    }

    private void triggerAlarmIntent(Context context, String action) {
        String alarmId = null;
        if (action.startsWith(KEY_ALARM)) {
            alarmId = action.replace(KEY_ALARM, "");
        } else if (action.startsWith(KEY_SNOOZE)) {
            alarmId = action.replace(KEY_SNOOZE, "");
        }

        Alarm alarm = AlarmManager.getAlarm(context, alarmId);
        if (alarm == null) return;

        if (alarm.getDays().isEmpty()) {
            alarm.setActivated(false);
            AlarmManager.updateAlarm(context, alarm);
        }

        Intent alarmIntent = alarm.getIntent();
        if (alarmIntent != null) {
            AlarmManager.setDeviceVolume(context, alarm.getVolume());
            alarmIntent.putExtra(AlarmManager.INTENT_ALARM_ID, alarmId);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmIntent);

            if (alarm.isActivated()) {
                AlarmManager.updateAlarm(context, alarm);
            }
        }
    }

}
