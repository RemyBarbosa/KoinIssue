package fr.radiofrance.alarm.datastore;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.net.URISyntaxException;

import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;

public class ConfigurationDatastore {

    // This Intent is used when Alarm is launch at time and has no intentUri, should be the alarm lock screen activity
    private static final String KEY_ALARM_DEFAULT_LAUNCH_INTENT = "rf.alarm.manager.alarm.default.launch.intent";
    // This Intent is used when alarm screen need to launch the app
    private static final String KEY_ALARM_APP_LAUNCH_INTENT = "rf.alarm.manager.alarm.app.launch.intent";
    // This Intent is used when system Clock info want to open the alarm edit screen
    private static final String KEY_ALARM_SHOW_EDIT_LAUNCH_INTENT = "rf.alarm.manager.alarm.show.edit.launch.intent";

    private static final String KEY_ALARM_LAST_EXECUTED = "rf.alarm.manager.alarm.last.executed";

    private final SharedPreferencesManager preferencesManager;

    public ConfigurationDatastore(final Context context) {
        this.preferencesManager = new SharedPreferencesManager(context);
    }

    public boolean setAlarmDefaultLaunchIntent(final Intent intent) {
        if (intent == null) {
            return false;
        }
        return preferencesManager.storeString(KEY_ALARM_DEFAULT_LAUNCH_INTENT, intent.toUri(0));
    }

    public Intent getAlarmDefaultLaunchIntent(final Intent defaultValue) {
        return getIntent(KEY_ALARM_DEFAULT_LAUNCH_INTENT, defaultValue);
    }

    public boolean setAlarmAppLaunchIntent(final Intent intent) {
        if (intent == null) {
            return false;
        }
        return preferencesManager.storeString(KEY_ALARM_APP_LAUNCH_INTENT, intent.toUri(0));
    }

    public Intent getAlarmAppLaunchIntent(final Intent defaultValue) {
        return getIntent(KEY_ALARM_APP_LAUNCH_INTENT, defaultValue);
    }

    public boolean setAlarmShowEditLaunchIntent(final Intent intent) {
        if (intent == null) {
            return false;
        }
        return preferencesManager.storeString(KEY_ALARM_SHOW_EDIT_LAUNCH_INTENT, intent.toUri(0));
    }

    public Intent getAlarmShowEditLaunchIntent(final Intent defaultValue) {
        return getIntent(KEY_ALARM_SHOW_EDIT_LAUNCH_INTENT, defaultValue);
    }

    public boolean setAlarmLastExecutedHash(final int hash) {
        return preferencesManager.storeInt(KEY_ALARM_LAST_EXECUTED, hash);
    }

    public int getAlarmLastExecutedHash() {
        return preferencesManager.getInt(KEY_ALARM_LAST_EXECUTED, -1);
    }

    private Intent getIntent(final String key, final Intent defaultValue) {
        if (!preferencesManager.contains(key)) {
            return defaultValue;
        }
        final String intentUri = preferencesManager.getString(key);
        if (TextUtils.isEmpty(intentUri)) {
            return defaultValue;
        }
        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            return defaultValue;
        }
    }

}
