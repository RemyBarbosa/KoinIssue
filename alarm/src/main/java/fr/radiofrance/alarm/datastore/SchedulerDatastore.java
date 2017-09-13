package fr.radiofrance.alarm.datastore;

import android.content.Context;

import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;

public class SchedulerDatastore {

    private static final String KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID = "fr.radiofrance.alarm.datastore.KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID";
    private static final String KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID = "fr.radiofrance.alarm.datastore.KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID";

    private final SharedPreferencesManager preferencesManager;

    public SchedulerDatastore(final Context context) {
        this.preferencesManager = new SharedPreferencesManager(context);
    }

    public void saveCurrentStandardAlarmId(final String alarmId) {
        preferencesManager.storeString(KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID, alarmId);
    }

    public String getCurrentStandardAlarmId() {
        return preferencesManager.getString(KEY_SCHEDULER_CURRENT_STANDARD_ALARM_ID);
    }

    public void saveCurrentSnoozeAlarmId(final String alarmId) {
        preferencesManager.storeString(KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID, alarmId);
    }

    public String getCurrentSnoozeAlarmId() {
        return preferencesManager.getString(KEY_SCHEDULER_CURRENT_SNOOZE_ALARM_ID);
    }

}
