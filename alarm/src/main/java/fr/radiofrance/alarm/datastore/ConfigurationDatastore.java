package fr.radiofrance.alarm.datastore;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.net.URISyntaxException;

import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;

public class ConfigurationDatastore {

    private static final String KEY_ALARM_APP_LAUNCH_INTENT = "rf.alarm.manager.alarm.app.launch.intent";
    private static final String KEY_ALARM_DEFAULT_LAUNCH_INTENT = "rf.alarm.manager.alarm.default.launch.intent";
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
        if (!preferencesManager.contains(KEY_ALARM_DEFAULT_LAUNCH_INTENT)) {
            return defaultValue;
        }
        final String intentUri = preferencesManager.getString(KEY_ALARM_DEFAULT_LAUNCH_INTENT);
        if (TextUtils.isEmpty(intentUri)) {
            return defaultValue;
        }
        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            return defaultValue;
        }
    }

    public boolean setAlarmAppLaunchIntent(final Intent intent) {
        if (intent == null) {
            return false;
        }
        return preferencesManager.storeString(KEY_ALARM_APP_LAUNCH_INTENT, intent.toUri(0));
    }

    public Intent getAlarmAppLaunchIntent(final Intent defaultValue) {
        if (!preferencesManager.contains(KEY_ALARM_APP_LAUNCH_INTENT)) {
            return defaultValue;
        }
        final String intentUri = preferencesManager.getString(KEY_ALARM_APP_LAUNCH_INTENT);
        if (TextUtils.isEmpty(intentUri)) {
            return defaultValue;
        }
        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            return defaultValue;
        }
    }

    public boolean setAlarmLastExecutedHash(final int hash) {
        return preferencesManager.storeInt(KEY_ALARM_LAST_EXECUTED, hash);
    }

    public int getAlarmLastExecutedHash() {
        return preferencesManager.getInt(KEY_ALARM_LAST_EXECUTED, -1);
    }

}
