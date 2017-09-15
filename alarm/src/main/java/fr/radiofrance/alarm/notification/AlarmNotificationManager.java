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
import android.text.format.DateUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.receiver.RfAlarmReceiver;
import fr.radiofrance.alarm.util.AlarmDateUtils;

public class AlarmNotificationManager {

    private static final String LOG_TAG = AlarmNotificationManager.class.getSimpleName();

    private static final int NOTIFICATION_ID = 56735;
    private static final String NOTIFICATION_CHANNEL_ID = "rfalarm_notification_channel";

    public static final String ACTION_SHOW_UPCOMING_ALARM_NOTIFICATION = "ACTION_SHOW_UPCOMING_ALARM_NOTIFICATION";

    public static final String EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY = "fr.radiofrance.alarm.EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY";
    public static final String EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY = "fr.radiofrance.alarm.EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY";
    public static final String EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY = "fr.radiofrance.alarm.EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY";

    private static final int PENDING_INTENT_REQUEST_CODE = 65827;
    private static final long NOTIFICATION_SHOW_TIME_BEFORE_MILLIS = 10 * DateUtils.MINUTE_IN_MILLIS;

    @NonNull
    private final Context context;
    @NonNull
    private final AlarmManager alarmManager;
    @NonNull
    private final NotificationManager notificationManager;

    public AlarmNotificationManager(@NonNull final Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.alarm_notif_label_channel),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void programNotification(final Alarm nextAlarm, final Alarm snoozedAlarm) {
        Log.d(LOG_TAG, "programNotification: ");
        cancelPendingIntent();

        if (nextAlarm == null && snoozedAlarm == null) {
            // TODO cancel pending intent
            return;
        }

        if (snoozedAlarm == null) {
            final long alarmScheduleDateMillis = AlarmDateUtils.getAlarmNextScheduleDate(nextAlarm).getTimeInMillis();
            final long notificationShowTimeMillis = alarmScheduleDateMillis - NOTIFICATION_SHOW_TIME_BEFORE_MILLIS;

            if (notificationShowTimeMillis < System.currentTimeMillis()) {
                showNotification(nextAlarm.getId(), alarmScheduleDateMillis, false);
                return;
            }


        }

    }

    public void showNotification(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
        Log.d(LOG_TAG, "showNotification: ");
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setContentTitle(context.getString(R.string.alarm_notif_soon_label))
                // TODO make icon configurable
                .setSmallIcon(R.drawable.ic_alarm_notification)
                .setContentText(getNotificationDate(alarmTimeMillis))
                .setShowWhen(false);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void hideNotification() {
        Log.d(LOG_TAG, "hideNotification: ");
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void cancelPendingIntent() {
        final PendingIntent pendingIntent = PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, getIntent(), PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            return;
        }
        pendingIntent.cancel();
    }

    private PendingIntent buildPendingIntent(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
        final Intent showNotificationIntent = getIntent();
        showNotificationIntent.putExtra(EXTRA_ALARM_NOTIFICATION_ALARM_ID_KEY, alarmId);
        showNotificationIntent.putExtra(EXTRA_ALARM_NOTIFICATION_TIME_MILLIS_KEY, alarmTimeMillis);
        showNotificationIntent.putExtra(EXTRA_ALARM_NOTIFICATION_IS_SNOOZE_KEY, isSnooze);
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, showNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Intent getIntent() {
        return new Intent(context, RfAlarmReceiver.class).setAction(context.getPackageName() + "." + ACTION_SHOW_UPCOMING_ALARM_NOTIFICATION);
    }

    private String getNotificationDate(final long dateMillis) {
        return new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date(dateMillis))
                + " "
                + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(new Date(dateMillis));
    }
}
