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

    public static final String LAUNCH_PENDING_INTENT_ACTION_PREFIXE_SNOOZE = "rf.alarm.action.lauch.snooze.";
    public static final String LAUNCH_PENDING_INTENT_ACTION_PREFIXE_ALARM = "rf.alarm.action.lauch.alarm.";
    public static final String LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID = "rf.alarm.extra.lauch.alarm.id";
    public static final String LAUNCH_PENDING_INTENT_EXTRA_ALARM_HASH = "rf.alarm.extra.lauch.alarm.hash";
    public static final String LAUNCH_PENDING_INTENT_EXTRA_IS_SNOOZE = "rf.alarm.extra.lauch.is.snooze";
    public static final int LAUNCH_PENDING_INTENT_REQUEST_CODE = 1778;

    public static final int SHOW_EDIT_PENDING_INTENT_REQUEST_CODE = 1779;


    enum IntentType {
        ACTIVITY, SERVICE, BROADCAST, UNRECOGNIZED
    }

    public static PendingIntent getPendingIntent(@NonNull final Context context, @NonNull final Alarm alarm, final boolean isSnooze) {
        return buildPendingIntent(context, alarm, isSnooze, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent getActivityShowPendingIntent(@NonNull final Context context, final Intent showIntent) {
        return PendingIntent.getActivity(context, SHOW_EDIT_PENDING_INTENT_REQUEST_CODE, showIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static boolean isPendingIntentAlive(@NonNull final Context context, @NonNull final Alarm alarm, final boolean isSnooze) {
        return AlarmIntentUtils.buildPendingIntent(context, alarm, isSnooze, PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static void cancelPendingIntent(@NonNull final Context context, @NonNull final Alarm alarm, final boolean isSnooze) {
        final PendingIntent pendingIntent = AlarmIntentUtils.buildPendingIntent(context, alarm, isSnooze, PendingIntent.FLAG_CANCEL_CURRENT);
        if (pendingIntent == null) {
            return;
        }
        pendingIntent.cancel();
    }

    private static PendingIntent buildPendingIntent(@NonNull final Context context, @NonNull final Alarm alarm, final boolean isSnooze, final int flags) {
        final Intent alarmLaunchIntent = alarm.getIntent();
        if (alarmLaunchIntent == null) {
            return null;
        }

        final String action = buildLaunchIntentAction(alarm, isSnooze);
        alarmLaunchIntent.setAction(action);

        alarmLaunchIntent.putExtra(LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID, alarm.getId());
        alarmLaunchIntent.putExtra(LAUNCH_PENDING_INTENT_EXTRA_IS_SNOOZE, isSnooze);
        alarmLaunchIntent.putExtra(LAUNCH_PENDING_INTENT_EXTRA_ALARM_HASH, buildLaunchIntentHash(action, AlarmDateUtils.getAlarmNextScheduleDate(alarm)));

        switch (getTypeOfIntent(alarmLaunchIntent)) {
            case ACTIVITY:
                alarmLaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return PendingIntent.getActivity(context, LAUNCH_PENDING_INTENT_REQUEST_CODE, alarmLaunchIntent, flags);
            case BROADCAST:
                return PendingIntent.getBroadcast(context, LAUNCH_PENDING_INTENT_REQUEST_CODE, alarmLaunchIntent, flags);
            case SERVICE:
                return PendingIntent.getService(context, LAUNCH_PENDING_INTENT_REQUEST_CODE, alarmLaunchIntent, flags);
            default:
                return null;
        }
    }

    private static int buildLaunchIntentHash(@NonNull String action, @NonNull Calendar calendar) {
        return (action + ":" + calendar.toString()).hashCode();
    }

    private static String buildLaunchIntentAction(@NonNull final Alarm alarm, final boolean isSnooze) {
        if (isSnooze) {
            return LAUNCH_PENDING_INTENT_ACTION_PREFIXE_SNOOZE + alarm.getId();
        } else {
            return LAUNCH_PENDING_INTENT_ACTION_PREFIXE_ALARM + alarm.getId();
        }
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
