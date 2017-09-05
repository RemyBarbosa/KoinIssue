package fr.radiofrance.alarm.datastore.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Set;

public class SharedPreferencesManager {

    private static final String LOG_TAG = SharedPreferencesManager.class.getSimpleName();

    // Could not be changed to be sure to recovery alarm from previous version
    private static final String PREFS_NAME = "prefs";

    @NonNull
    private final SharedPreferences mSettings;

    public SharedPreferencesManager(@NonNull final Context context) {
        mSettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean contains(String key) {
        return mSettings.contains(key);
    }

    public String getString(String key) {
        return mSettings.getString(key, "");
    }

    public boolean storeString(String key, String value) {
        try {
            final SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(key, value);
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Set<String> getStringSet(String key, Set<String> defValue) {
        return mSettings.getStringSet(key, defValue);
    }

    public boolean storeStringSet(String key, Set<String> value) {
        try {
            final SharedPreferences.Editor editor = mSettings.edit();
            editor.putStringSet(key, value);
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getInt(String key, int defValue) {
        return mSettings.getInt(key, defValue);
    }

    public boolean storeInt(String key, int value) {
        try {
            final SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(key, value);
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getLong(String key, long defValue) {
        return mSettings.getLong(key, defValue);
    }

    public boolean storeLong(String key, long value) {
        try {
            final SharedPreferences.Editor editor = mSettings.edit();
            editor.putLong(key, value);
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getBoolean(String key) {
        return mSettings.getBoolean(key, false);
    }

    public boolean storeBoolean(String key, boolean value) {
        try {
            final SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(key, value);
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove(String key) {
        try {
            final SharedPreferences.Editor editor = mSettings.edit();
            editor.remove(key);
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void flush() {
        final SharedPreferences.Editor editor = mSettings.edit();
        editor.clear();
        editor.apply();
    }

}
