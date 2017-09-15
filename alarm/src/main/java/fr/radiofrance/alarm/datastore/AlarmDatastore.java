package fr.radiofrance.alarm.datastore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;
import fr.radiofrance.alarm.model.Alarm;

public class AlarmDatastore {

    // Could not be changed to be sure to recovery alarm from previous version
    private static final String KEY_ALL_ALARM_IDS = "AlarmsList";
    private static final String KEY_ALARM_PREFIXE = "Alarm";

    @NonNull
    private final SharedPreferencesManager preferencesManager;
    @NonNull
    private final Gson gson;

    public AlarmDatastore(@NonNull final Context context) {
        this.preferencesManager = new SharedPreferencesManager(context);
        this.gson = new Gson();
    }

    @Nullable
    public Alarm getAlarm(@Nullable final String alarmId) {
        final String alarmKey = getAlarmKey(alarmId);
        if (TextUtils.isEmpty(alarmKey) || !preferencesManager.contains(alarmKey)) {
            return null;
        }
        final String alarmString = preferencesManager.getString(alarmKey);
        if (TextUtils.isEmpty(alarmString)) {
            return null;
        }
        return gson.fromJson(alarmString, Alarm.class);
    }

    public boolean saveAlarm(@Nullable final Alarm alarm) {
        if (alarm == null) {
            return false;
        }
        final String alarmId = alarm.getId();

        // Updating the list of All AlarmIds
        final Set<String> alarmIds = getAllAlarmIds();
        if (!alarmIds.contains(alarmId)) {
            alarmIds.add(alarmId);
            if (!preferencesManager.storeStringSet(KEY_ALL_ALARM_IDS, alarmIds)) {
                return false;
            }
        }

        // Saving the Alarm
        final String alarmKey = getAlarmKey(alarmId);
        return preferencesManager.storeString(alarmKey, gson.toJson(alarm));
    }

    public boolean removeAlarm(@Nullable final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }
        // Updating the list of All AlarmIds
        final Set<String> alarmIds = getAllAlarmIds();
        if (alarmIds.contains(alarmId)) {
            alarmIds.remove(alarmId);
            if (!preferencesManager.storeStringSet(KEY_ALL_ALARM_IDS, alarmIds)) {
                return false;
            }
        }

        // Removing the Alarm
        final String alarmKey = getAlarmKey(alarmId);
        return preferencesManager.remove(alarmKey);
    }

    @NonNull
    public Set<String> getAllAlarmIds() {
        return new HashSet<>(preferencesManager.getStringSet(KEY_ALL_ALARM_IDS, new HashSet<String>()));
    }

    @Nullable
    private String getAlarmKey(@Nullable final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return null;
        }
        return KEY_ALARM_PREFIXE + alarmId;
    }

}
