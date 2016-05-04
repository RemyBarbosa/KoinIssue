package fr.radiofrance.alarm.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.fragment.AlarmSettingFragment;
import fr.radiofrance.alarm.fragment.AlarmWakeUpFragment;
import fr.radiofrance.analytics.AtInternetHelper;
import fr.radiofrance.analytics.Xiti;
import fr.radiofrance.androidtoolbox.constant.Constants;
import fr.radiofrance.androidtoolbox.io.PrefsTools;
import fr.radiofrance.androidtoolbox.log.DebugLog;
import fr.radiofrance.model.station.Station;

public class AlarmActivity extends FragmentActivity {

    private static final String TAG = AlarmActivity.class.getName();

    public static final String DEFAULT_RADIO     = "AlarmActivityDefaultRadio";
    public static final String DEFAULT_LOCALE    = "AlarmActivityDefaultLocale";
    public static final String WAKE_UP           = "AlarmActivityWakeUp";
    public static final String LOCK_ORIENTATION  = "AlarmActivityLockOrientation";
    public static final String ALARM_LOCALE      = "com.radiofrance.radio.radiofrance.RadioFranceAlarmLocale";
    public static final String ALARM_ACTIVATED   = "com.radiofrance.radio.radiofrance.RadioFranceAlarmIsActivated";
    public static final String ALARM_TIMESTAMP   = "com.radiofrance.radio.radiofrance.RadioFranceAlarmTimestamp";
    private boolean            mWakeupMode       = false;
    Station station;
    public static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        mWakeupMode = getIntent().getBooleanExtra(WAKE_UP, false);
        setContentFragment();

        Calendar now = Calendar.getInstance();
        setupBackground(now);

        if (PrefsTools.hasKey(this, Constants.PREF_SELECTED_STATION)) {
            String json = PrefsTools.getString(this, Constants.PREF_SELECTED_STATION);
            station =  new Gson().fromJson(json, Station.class);
        }

        if (station != null){
            AtInternetHelper.sendScreenTag(Xiti.formatLabel(station.getTitle()),
                    getString(R.string.xiti_configuration),
                    getString(R.string.xiti_alarm_clock),
                    getString(R.string.xiti_clock_screen)
            );
        }


    }

    public static final void logDate(String presentation, Calendar cal){
        DebugLog.d(TAG, presentation + " - date : " + DISPLAY_DATE_FORMAT.format(cal.getTime()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWakeupMode = intent.getBooleanExtra(WAKE_UP, false);
        setContentFragment();
    }

    private void setContentFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mWakeupMode ?
                        new AlarmWakeUpFragment() :
                        new AlarmSettingFragment())
                .commit();
    }

    private Drawable generateAlarmBackgroundDrawable(Calendar calendar) {
        // Taken from the corresponding algorithm in the iOS project
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        int[] colors = new int[2];
        int to14 = calendar.get(Calendar.HOUR_OF_DAY) <= 2 ? calendar.get(Calendar.HOUR_OF_DAY) + 24 : calendar.get(Calendar.HOUR_OF_DAY);
        to14 = to14 - 14;
        to14 = to14 >= 0 ? to14 : -to14;
        int to8 = to14 - 6;
        to8 = to8 >= 0 ? to8 : -to8;
        float r0 = (0.2f + 0.2f * (12 - to14) / 12.0f);
        float g0 = (0.3f + 0.3f * (12 - to14) / 12.0f);
        float b0 = (0.3f + 1.4f * (12 - to14) / 12.0f);
        float r1 = (0.1f + 0.8f * (6 - to8) / 12.0f + 0.2f * (12 - to14) / 12.0f);
        float g1 = (0.1f + 0.6f * (6 - to8) / 12.0f + 0.2f * (12 - to14) / 12.0f);
        float b1 = (0.2f + 0.2f * (6 - to8) / 12.0f + 1.1f * (12 - to14) / 12.0f);
        r0 = Math.min(r0, 1.0f);
        g0 = Math.min(g0, 1.0f);
        b0 = Math.min(b0, 1.0f);
        r1 = Math.min(r1, 1.0f);
        g1 = Math.min(g1, 1.0f);
        b1 = Math.min(b1, 1.0f);
        colors[0] = Color.rgb((int) (255 * r0), (int) (255 * g0), (int) (255 * b0));
        colors[1] = Color.rgb((int) (255 * r1), (int) (255 * g1), (int) (255 * b1));
        return new GradientDrawable(Orientation.TOP_BOTTOM, colors);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public void setupBackground(Calendar calendar) {
        Drawable background = generateAlarmBackgroundDrawable(calendar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            findViewById(R.id.fragment_container).setBackground(background);
        } else {
            findViewById(R.id.fragment_container).setBackgroundDrawable(background);
        }
    }

    public static boolean getAlarmActivated(Context context, boolean defaultValue) {
        return PrefsTools.getBool(context, ALARM_ACTIVATED, defaultValue);
    }

    public boolean getAlarmActivated(boolean defaultValue) {
        return PrefsTools.getBool(getApplicationContext(), ALARM_ACTIVATED, defaultValue);
    }

    public void setAlarmActivated(boolean activated) {
        PrefsTools.setBool(getApplicationContext(), ALARM_ACTIVATED, activated);
    }

    public static Calendar getAlarmTime(Context context) {
        long alarmTime = PrefsTools.getLong(context, ALARM_TIMESTAMP, 0);
        Calendar calendar = Calendar.getInstance();

        if (alarmTime != 0){
            calendar.setTimeInMillis(alarmTime);
            // Used to compute date sets
            calendar.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        }

        return calendar;

    }

    public static void setAlarmTime(Context context, Calendar alarmTime) {
        PrefsTools.setLong(context, ALARM_TIMESTAMP, alarmTime.getTimeInMillis());
    }
}
