package fr.radiofrance.alarm.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Wrapper for SharedPreferences Android system
 * Assumes that developers most of time don't need several PREFS_NAME for 1 app
 */
public class PrefsUtils {

    private static final String TAG = "PrefsTools";

    private static final String PREFS_NAME = "prefs";

    private static SharedPreferences getPrefs(final Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void removeKey(final Context ctx, final String key) {
        final SharedPreferences settings = getPrefs(ctx);
        final SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

    public static int getInteger(final Context ctx, final String key, final int defaultValue) {
        return getPrefs(ctx).getInt(key, defaultValue);
    }

    public static void setInteger(final Context ctx, final String key, final int val) {
        final SharedPreferences settings = getPrefs(ctx);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, val);
        editor.apply();
    }

    public static String getString(final Context ctx, final String key, final String defaultValue) {
        return getPrefs(ctx).getString(key, defaultValue);
    }

    public static void setString(final Context ctx, final String key, final String val) {
        final SharedPreferences settings = getPrefs(ctx);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static Set<String> getStringSet(final Context ctx, final String key, final Set<String> defaultValue) {
        return getPrefs(ctx).getStringSet(key, defaultValue);
    }

    public static void setStringSet(final Context ctx, final String key, final Set<String> val) {
        final SharedPreferences settings = getPrefs(ctx);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(key, val);
        editor.apply();
    }

    public static boolean hasKey(final Context ctx, final String key) {
        return getPrefs(ctx).contains(key);
    }

}
