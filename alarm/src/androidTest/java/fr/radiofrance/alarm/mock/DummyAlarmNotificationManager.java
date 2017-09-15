package fr.radiofrance.alarm.mock;

import android.content.Context;
import android.support.annotation.NonNull;

import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.notification.AlarmNotificationManager;


public class DummyAlarmNotificationManager extends AlarmNotificationManager {

    public DummyAlarmNotificationManager(@NonNull final Context context) {
        super(context);
    }

    public void programNotification(final Alarm nextAlarm, final Alarm snoozedAlarm) {
    }

    public void showNotification(@NonNull final String alarmId, final long alarmTimeMillis, final boolean isSnooze) {
    }

    public void hideNotification() {
    }
}
