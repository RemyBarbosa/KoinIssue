package fr.radiofrance.alarm.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import java.util.List;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;

/**
 * Created by mondon on 13/05/16.
 */
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
            List<Alarm> alarms = AlarmManager.getInstance().getAllAlarms();
            if (alarms != null) {
                for (Alarm alarm : alarms) {
                    AlarmManager.getInstance().addAlarm(alarm);
                }
            }
        } else if (action.startsWith(KEY_ALARM) || action.startsWith(KEY_SNOOZE)) {
            showAlarmActivity(context, action);
        }
    }

    private void showAlarmActivity(Context context, String action) {
        String alarmId = null;
        if (action.startsWith(KEY_ALARM)) {
            alarmId = action.replace(KEY_ALARM, "");
        } else if (action.startsWith(KEY_SNOOZE)) {
            alarmId = action.replace(KEY_SNOOZE, "");
        }

        Alarm alarm = AlarmManager.getInstance().getAlarm(alarmId);
        if (alarm == null) return;

        Intent alarmIntent = alarm.getIntent();
        if (alarmIntent != null) {
            AlarmManager.getInstance().setDeviceVolume(alarm.getVolume());
            alarmIntent.putExtra(AlarmManager.INTENT_ALARM_ID, alarmId);
            context.startActivity(alarmIntent);

            if (!AlarmManager.getInstance().isAlarmAdded(alarmId)) {
                AlarmManager.getInstance().addAlarm(alarm);
            }
        }
    }

}
