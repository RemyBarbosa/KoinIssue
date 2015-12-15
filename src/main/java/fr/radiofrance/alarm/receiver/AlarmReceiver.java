package fr.radiofrance.alarm.receiver;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import fr.radiofrance.alarm.activity.AlarmActivity;
import fr.radiofrance.alarm.fragment.AlarmSettingFragment;
import fr.radiofrance.androidtoolbox.io.PrefsTools;
import fr.radiofrance.androidtoolbox.log.DebugLog;


public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = "AlarmReceiver";

    private static final String ALARM_TRIGGER_TAG = AlarmReceiver.class.getSimpleName() + "ALARM_TRIGGER_TAG";
    private static final String SNOOZE_TRIGGER_TAG = AlarmReceiver.class.getSimpleName() + "SNOOZE_TRIGGER_TAG";
    private final static int REGULAR_ALARM_CODE = 0;
    private final static int SNOOZE_ALARM_CODE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarm(context);
        } else if (intent.getAction().equals(ALARM_TRIGGER_TAG) ||intent.getAction().equals(SNOOZE_TRIGGER_TAG)) {
            DebugLog.d(TAG, "ALARM RECEIVED");
            runAlarm(context, intent.getAction());
        }
    }

    private void runAlarm(Context context, String action) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra(AlarmActivity.WAKE_UP, true);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);

        if (action.equals(ALARM_TRIGGER_TAG)){
            scheduleNextAlarm(context);
        }
    }

    private void scheduleNextAlarm(Context context) {
        rescheduleAlarm(context);
    }

    private void rescheduleAlarm(Context context) {
        if (PrefsTools.getBool(context, AlarmActivity.ALARM_ACTIVATED, false)) {
            Calendar cal = AlarmActivity.getAlarmTime(context);

            // Reschedule only if Alarm new time is passed
            if (cal.getTimeInMillis()<=Calendar.getInstance().getTimeInMillis()){
                Calendar calNext = AlarmSettingFragment.timeAfterNow(cal);
                AlarmActivity.logDate("NEXT ALARM TIME : ", calNext);

                AlarmActivity.setAlarmTime(context, calNext);
                setAlarm(context, calNext);
            }
        } else {
            cancelAlarm(context);
        }
    }

    public static void setAlarm(Context context, Calendar time) {
        Calendar calculatedTime = AlarmSettingFragment.timeAfterNow(time);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ALARM_TRIGGER_TAG);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getBroadcast(context, REGULAR_ALARM_CODE, intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calculatedTime.getTimeInMillis(), pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, calculatedTime.getTimeInMillis(), pi);
        }
    }

    public static void snoozeAlarm(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 10);
        // Used to compute date sets
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        AlarmActivity.logDate("SNOOZE DATE", cal);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(SNOOZE_TRIGGER_TAG);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getBroadcast(context, SNOOZE_ALARM_CODE, intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }

    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ALARM_TRIGGER_TAG);
        PendingIntent sender = PendingIntent.getBroadcast(context, REGULAR_ALARM_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}
