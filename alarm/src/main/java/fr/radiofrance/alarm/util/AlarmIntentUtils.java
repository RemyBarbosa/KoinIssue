package fr.radiofrance.alarm.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.Calendar;

import fr.radiofrance.alarm.model.Alarm;

public abstract class AlarmIntentUtils {

    public static final int LAUNCH_PENDING_INTENT_REQUEST_CODE = 1778;
    public static final int SHOW_EDIT_PENDING_INTENT_REQUEST_CODE = 1779;

    public static final String LAUNCH_PENDING_INTENT_ACTION_WAKEUP = "ALARM_WAKEUP_RADIO";
    public static final String LAUNCH_PENDING_INTENT_ACTION_SNOOZE = "ALARM_SNOOZE_RADIO";

    public static final String LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID = "rf.alarm.extra.lauch.alarm.id";
    public static final String LAUNCH_PENDING_INTENT_EXTRA_ALARM_HASH = "rf.alarm.extra.lauch.alarm.hash";
    public static final String LAUNCH_PENDING_INTENT_EXTRA_IS_SNOOZE = "rf.alarm.extra.lauch.is.snooze";

    enum IntentType {
        ACTIVITY, SERVICE, BROADCAST, UNRECOGNIZED
    }

    public static PendingIntent getActivityShowPendingIntent(@NonNull final Context context, final Intent showIntent) {
        return PendingIntent.getActivity(context, SHOW_EDIT_PENDING_INTENT_REQUEST_CODE, showIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    // TODO buildAlarmIntent: create an AlarmIntentBuilder with optional method completeAlarmExtras()
    public static Intent buildAlarmIntent(@NonNull final Alarm alarm, final boolean isSnooze) {
        final Intent alarmLaunchIntent = alarm.getIntent();
        if (alarmLaunchIntent == null) {
            return null;
        }
        // Add Alarm extras to Intent
        alarmLaunchIntent.putExtra(LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID, alarm.getId());
        alarmLaunchIntent.putExtra(LAUNCH_PENDING_INTENT_EXTRA_IS_SNOOZE, isSnooze);
        alarmLaunchIntent.putExtra(LAUNCH_PENDING_INTENT_EXTRA_ALARM_HASH, buildLaunchIntentHash(alarm.getId(), isSnooze, AlarmDateUtils.getAlarmNextScheduleDate(alarm)));

        return alarmLaunchIntent;
    }

    public static PendingIntent getPendingIntent(@NonNull final Context context, Intent alarmIntent) {
        if (alarmIntent == null) {
            return null;
        }
        return buildPendingIntent(context, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static boolean isPendingIntentAlive(@NonNull final Context context, Intent alarmIntent) {
        if (alarmIntent == null) {
            return false;
        }
        return AlarmIntentUtils.buildPendingIntent(context, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static void cancelPendingIntent(@NonNull final Context context, Intent alarmIntent) {
        if (alarmIntent == null) {
            return;
        }
        final PendingIntent pendingIntent = AlarmIntentUtils.buildPendingIntent(context, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (pendingIntent == null) {
            return;
        }
        pendingIntent.cancel();
    }

    public static String buildActionWithPackageName(@NonNull final Context context, @NonNull String action) {
        return context.getPackageName() + "." + action;
    }

    private static PendingIntent buildPendingIntent(@NonNull final Context context, @NonNull Intent alarmIntent, final int flags) {
        if (alarmIntent.getBooleanExtra(LAUNCH_PENDING_INTENT_EXTRA_IS_SNOOZE, false)) {
            alarmIntent.setAction(buildActionWithPackageName(context, LAUNCH_PENDING_INTENT_ACTION_SNOOZE));
        } else {
            alarmIntent.setAction(buildActionWithPackageName(context, LAUNCH_PENDING_INTENT_ACTION_WAKEUP));
        }

        switch (getTypeOfIntent(alarmIntent)) {
            case ACTIVITY:
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return PendingIntent.getActivity(context, LAUNCH_PENDING_INTENT_REQUEST_CODE, alarmIntent, flags);
            case BROADCAST:
                return PendingIntent.getBroadcast(context, LAUNCH_PENDING_INTENT_REQUEST_CODE, alarmIntent, flags);
            case SERVICE:
                return PendingIntent.getService(context, LAUNCH_PENDING_INTENT_REQUEST_CODE, alarmIntent, flags);
            default:
                return null;
        }
    }

    private static int buildLaunchIntentHash(@NonNull String alarmId, boolean isSnooze, @NonNull Calendar calendar) {
        return (alarmId + ":" + isSnooze + ":" + calendar.toString()).hashCode();
    }

    private static IntentType getTypeOfIntent(@NonNull final Intent intent) {
        try {
            if (intent.getComponent() == null) {
                return IntentType.UNRECOGNIZED;
            }
            final Class<?> act = Class.forName(intent.getComponent().getClassName());
            if (Activity.class.isAssignableFrom(act)) {
                return IntentType.ACTIVITY;
            }
            if (BroadcastReceiver.class.isAssignableFrom(act)) {
                return IntentType.BROADCAST;
            }
            if (Service.class.isAssignableFrom(act)) {
                return IntentType.SERVICE;
            }
            return IntentType.UNRECOGNIZED;
        } catch (ClassNotFoundException e) {
            return IntentType.UNRECOGNIZED;
        }
    }

}
