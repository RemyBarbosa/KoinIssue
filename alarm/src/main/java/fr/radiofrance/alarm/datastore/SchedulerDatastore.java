package fr.radiofrance.alarm.datastore;

import android.content.Context;

import fr.radiofrance.alarm.datastore.model.ScheduleData;
import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;

public class SchedulerDatastore {

    private static final String KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID = "fr.radiofrance.alarm.datastore.KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID";
    private static final String KEY_SCHEDULER_CURRENT_STANDARD_SCHEDULE_TIME_MILLIS = "fr.radiofrance.alarm.datastore.KEY_SCHEDULER_CURRENT_STANDARD_SCHEDULE_TIME_MILLIS";
    private static final String KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID = "fr.radiofrance.alarm.datastore.KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID";
    private static final String KEY_SCHEDULER_CURRENT_SNOOZE_SCHEDULE_TIME_MILLIS = "fr.radiofrance.alarm.datastore.KEY_SCHEDULER_CURRENT_SNOOZE_SCHEDULE_TIME_MILLIS";

    private final SharedPreferencesManager preferencesManager;

    public SchedulerDatastore(final Context context) {
        this.preferencesManager = new SharedPreferencesManager(context);
    }

    public void saveCurrentStandard(final ScheduleData data) {
        if (data == null) {
            preferencesManager.remove(KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID);
            preferencesManager.remove(KEY_SCHEDULER_CURRENT_STANDARD_SCHEDULE_TIME_MILLIS);
            return;
        }
        preferencesManager.storeString(KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID, data.alarmId);
        preferencesManager.storeLong(KEY_SCHEDULER_CURRENT_STANDARD_SCHEDULE_TIME_MILLIS, data.scheduleTimeMillis);
    }

    public ScheduleData getCurrentStandard() {
        if (!preferencesManager.contains(KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID)) {
            return null;
        }
        return new ScheduleData(preferencesManager.getString(KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID),
                preferencesManager.getLong(KEY_SCHEDULER_CURRENT_STANDARD_SCHEDULE_TIME_MILLIS, 0L));
    }

    public void saveCurrentSnooze(final ScheduleData data) {
        if (data == null) {
            preferencesManager.remove(KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID);
            preferencesManager.remove(KEY_SCHEDULER_CURRENT_SNOOZE_SCHEDULE_TIME_MILLIS);
            return;
        }
        preferencesManager.storeString(KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID, data.alarmId);
        preferencesManager.storeLong(KEY_SCHEDULER_CURRENT_SNOOZE_SCHEDULE_TIME_MILLIS, data.scheduleTimeMillis);
    }

    public ScheduleData getCurrentSnooze() {
        if (!preferencesManager.contains(KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID)) {
            return null;
        }
        return new ScheduleData(preferencesManager.getString(KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID),
                preferencesManager.getLong(KEY_SCHEDULER_CURRENT_SNOOZE_SCHEDULE_TIME_MILLIS, 0L));
    }

}
