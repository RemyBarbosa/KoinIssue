package fr.radiofrance.alarm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;
import java.util.Set;

/**
 * Created by kwaky on 12/03/15.
 * Wrapper for SharedPreferences Android system
 * Assumes that developers most of time don't need several PREFS_NAME for 1 app
 */
public class PrefsUtils {

    public static final String TAG = "PrefsTools";
    public static final String PREFS_NAME = "prefs";

    private static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void removeKey(Context ctx, String key) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

    public static boolean getBool(Context ctx, String key) {
        return getPrefs(ctx).getBoolean(key, false);
    }

    public static boolean getBool(Context ctx, String key, boolean defaultValue) {
        return getPrefs(ctx).getBoolean(key, defaultValue);
    }

    public static void setBool(Context ctx, String key, boolean val) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public static float getFloat(Context ctx, String key) {
        return getPrefs(ctx).getFloat(key, 0);
    }

    public static void setFloat(Context ctx, String key, float val) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, val);
        editor.apply();
    }

    public static int getInteger(Context ctx, String key) {
        return getPrefs(ctx).getInt(key, 0);
    }

    public static int getInteger(Context ctx, String key, int defaultValue) {
        return getPrefs(ctx).getInt(key, defaultValue);
    }

    public static void setInteger(Context ctx, String key, int val) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, val);
        editor.apply();
    }

    public static long getLong(Context ctx, String key, long defaultValue) {
        return getPrefs(ctx).getLong(key, defaultValue);
    }

    public static void setLong(Context ctx, String key, long val) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, val);
        editor.apply();
    }

    public static String getString(Context ctx, String key) {
        return getPrefs(ctx).getString(key, null);
    }

    public static String getString(Context ctx, String key, String defaultValue) {
        return getPrefs(ctx).getString(key, defaultValue);
    }

    public static void setString(Context ctx, String key, String val) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static Set<String> getStringSet(Context ctx, String key, Set<String> defaultValue) {
        return getPrefs(ctx).getStringSet(key, defaultValue);
    }

    public static void setStringSet(Context ctx, String key, Set<String> val) {
        SharedPreferences settings = getPrefs(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(key, val);
        editor.apply();
    }

    public static boolean hasKey(Context ctx, String key) {
        return getPrefs(ctx).contains(key);
    }

    /**
     * List all preferences saved from prefs file name PREFS_NAME
     *
     * @param ctx app Context
     */
    public static void showPrefsValues(Context ctx) {
        SharedPreferences settings = getPrefs(ctx);

        Map<String, ?> map = settings.getAll();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            Log.d(TAG, "key : " + key);
            Log.d(TAG, "type : " + map.get(key).getClass().getName());
            Log.d(TAG, "value : " + map.get(key));
        }
    }

    public static void clearPrefs(Context ctx) {
        getPrefs(ctx).edit().clear().commit();
    }

    /**
     * Get the boolean value of a settings created by a PreferenceActivity or PreferenceFragment
     * using the key put in parameter
     *
     * @param ctx
     * @param key
     * @return boolean value
     */
    public static boolean getScreenSettingsBooleanValue(Context ctx, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(key, false);
    }

    /**
     * Get the boolean value of a settings created by a PreferenceActivity or PreferenceFragment
     * using the key put in parameter
     *
     * @param ctx
     * @param key
     * @param defaultValue
     * @return boolean value
     */
    public static boolean getScreenSettingsBooleanValue(Context ctx, String key, boolean defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(key, defaultValue);
    }

    /**
     * Get the String value of a settings created by a PreferenceActivity or PreferenceFragment
     * using the key put in parameter
     *
     * @param ctx
     * @param key
     * @return String value
     */
    public static String getScreenSettingsStringValue(Context ctx, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getString(key, null);
    }

    /**
     * Set the String value of a settings created by a PreferenceActivity or PreferenceFragment
     * using the key put in parameter
     *
     * @param ctx   the current context
     * @param key   the key searched
     * @param value the value to set
     */
    public static void setScreenSettingsStringValue(Context ctx, String key, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putString(key, value).apply();
    }

    /**
     * Get the int value of a settings created by a PreferenceActivity or PreferenceFragment
     * using the key put in parameter
     *
     * @param ctx
     * @param key
     * @return int value
     */
    public static int getScreenSettingsIntValue(Context ctx, String key, int defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getInt(key, defaultValue);
    }

}
