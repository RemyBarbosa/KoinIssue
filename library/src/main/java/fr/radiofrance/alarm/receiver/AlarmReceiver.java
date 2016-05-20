package fr.radiofrance.alarm.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import fr.radiofrance.alarm.manager.AlarmManager;

/**
 * Created by mondon on 13/05/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String KEY_ALARM = AlarmReceiver.class.getSimpleName() + "KEY_ALARM";
    public static final String KEY_SNOOZE = AlarmReceiver.class.getSimpleName() + "KEY_SNOOZE";
    public static final int ALARM_CODE = 0;
    public static final int SNOOZE_CODE = 1;

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AlarmManager alarmManager = new AlarmManager(context);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            alarmManager.setAlarm(alarmManager.getAlarmIntent(), alarmManager.getAlarmDays(),
                    alarmManager.getAlarmHours(), alarmManager.getAlarmMinutes());
        } else if (KEY_ALARM.equals(action) || KEY_SNOOZE.equals(action)) {
            showAlarmActivity(context, alarmManager, action);
        }
    }

    private void showAlarmActivity(Context context, AlarmManager alarmManager, String action) {
        Intent alarmIntent = alarmManager.getAlarmIntent();
        if (alarmIntent != null) {
            alarmManager.setSystemVolume(alarmManager.getAlarmVolume());
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(alarmIntent);
        }

        if (action.equals(KEY_ALARM)) {
            alarmManager.setAlarm(alarmIntent, alarmManager.getAlarmDays(),
                    alarmManager.getAlarmHours(), alarmManager.getAlarmMinutes());
        }
    }

}
