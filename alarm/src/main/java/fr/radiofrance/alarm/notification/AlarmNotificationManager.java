package fr.radiofrance.alarm.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.datastore.model.ScheduleData;
import fr.radiofrance.alarm.receiver.RfAlarmReceiver;
import fr.radiofrance.alarm.util.AlarmIntentUtils;

public class AlarmNotificationManager {

    private static final String LOG_TAG = AlarmNotificationManager.class.getSimpleName();

    private static final int NOTIFICATION_ID = 56735;
    private static final String NOTIFICATION_CHANNEL_ID = "rfalarm_notification_channel";

    public static final String ACTION_ALARM_NOTIFICATION_SHOW_UPCOMING = "ACTION_ALARM_NOTIFICATION_SHOW_UPCOMING";
    public static final String ACTION_ALARM_NOTIFICATION_CANCEL = "ACTION_ALARM_NOTIFICATION_CANCEL";

    public static final String EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY = "fr.radiofrance.alarm.EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY";
    public static final String EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY = "fr.radiofrance.alarm.EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY";
    public static final String EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY = "fr.radiofrance.alarm.EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY";

    private static final int PENDING_INTENT_SHOW_REQUEST_CODE = 65827;
    private static final int PENDING_INTENT_CANCEL_REQUEST_CODE = 65828;
    private static final long NOTIFICATION_SHOW_TIME_BEFORE_MILLIS = DateUtils.HOUR_IN_MILLIS;

    @NonNull
    private final Context context;
    @NonNull
    private final AlarmManager alarmManager;
    @NonNull
    private final NotificationManager notificationManager;

    private String lastAlarmIdShown;

    public AlarmNotificationManager(@NonNull final Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.alarm_notif_label_channel),
                    NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void programNotification(ScheduleData standardAlarmScheduled, ScheduleData snoozeAlarmScheduled) {
        cancelPendingIntent();

        if (snoozeAlarmScheduled == null || snoozeAlarmScheduled.scheduleTimeMillis < System.currentTimeMillis()) {
            if (standardAlarmScheduled == null || standardAlarmScheduled.scheduleTimeMillis < System.currentTimeMillis()) {
                return;
            }
            programNotification(standardAlarmScheduled.alarmId, standardAlarmScheduled.scheduleTimeMillis, false);
            return;
        }

        if (standardAlarmScheduled == null || standardAlarmScheduled.scheduleTimeMillis < System.currentTimeMillis()) {
            programNotification(snoozeAlarmScheduled.alarmId, snoozeAlarmScheduled.scheduleTimeMillis, true);
            return;
        }

        if (standardAlarmScheduled.scheduleTimeMillis < snoozeAlarmScheduled.scheduleTimeMillis) {
            programNotification(standardAlarmScheduled.alarmId, standardAlarmScheduled.scheduleTimeMillis, false);
        } else {
            programNotification(snoozeAlarmScheduled.alarmId, snoozeAlarmScheduled.scheduleTimeMillis, true);
        }

    }

    public void showNotification(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setContentTitle(context.getString(R.string.alarm_notif_soon_label))
                .setSmallIcon(R.drawable.ic_alarm_notification)
                .setContentText(getNotificationDate(alarmTimeMillis))
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .addAction(R.drawable.ic_notif_cancel, context.getString(R.string.alarm_notif_cancel_action), buildActionCancelPendingIntent(alarmId, alarmTimeMillis, isSnooze))
                .setShowWhen(false);

        lastAlarmIdShown = alarmId;
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
        lastAlarmIdShown = null;
    }

    public boolean isLastNotificationShown(final String alarmId) {
        if (alarmId == null || lastAlarmIdShown == null) {
            return false;
        }
        return alarmId.equals(lastAlarmIdShown);
    }

    public boolean shouldShowNotificationNow(final long alarmTimeMillis) {
        final long notificationShowTimeMillis = alarmTimeMillis - NOTIFICATION_SHOW_TIME_BEFORE_MILLIS;
        return notificationShowTimeMillis < System.currentTimeMillis();
    }

    private void programNotification(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
        if (alarmTimeMillis < System.currentTimeMillis()) {
            return;
        }
        if (shouldShowNotificationNow(alarmTimeMillis)) {
            // Show now
            showNotification(alarmId, alarmTimeMillis, isSnooze);
            return;
        }

        // Program notification at time
        final PendingIntent pendingIntent = buildShowPendingIntent(alarmId, alarmTimeMillis, false);
        final long notificationShowTimeMillis = alarmTimeMillis - NOTIFICATION_SHOW_TIME_BEFORE_MILLIS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationShowTimeMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationShowTimeMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationShowTimeMillis, pendingIntent);
        }
    }

    private void cancelPendingIntent() {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, PENDING_INTENT_SHOW_REQUEST_CODE, getIntent(ACTION_ALARM_NOTIFICATION_SHOW_UPCOMING), PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            return;
        }
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private PendingIntent buildShowPendingIntent(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
        final Intent showNotificationIntent = getIntent(ACTION_ALARM_NOTIFICATION_SHOW_UPCOMING);
        showNotificationIntent.putExtra(EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY, alarmId);
        showNotificationIntent.putExtra(EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY, alarmTimeMillis);
        showNotificationIntent.putExtra(EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY, isSnooze);
        return PendingIntent.getBroadcast(context, PENDING_INTENT_SHOW_REQUEST_CODE, showNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent buildActionCancelPendingIntent(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
        final Intent actionCancelIntent = getIntent(ACTION_ALARM_NOTIFICATION_CANCEL);
        actionCancelIntent.putExtra(EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY, alarmId);
        actionCancelIntent.putExtra(EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY, alarmTimeMillis);
        actionCancelIntent.putExtra(EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY, isSnooze);
        return PendingIntent.getBroadcast(context, PENDING_INTENT_CANCEL_REQUEST_CODE, actionCancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Intent getIntent(final String action) {
        return new Intent(context, RfAlarmReceiver.class).setAction(AlarmIntentUtils.buildActionWithPackageName(context, action));
    }

    private String getNotificationDate(final long dateMillis) {
        return new SimpleDateFormat(context.getString(R.string.alarm_notif_date_format), Locale.getDefault()).format(new Date(dateMillis));
    }
}
