package fr.radiofrance.alarm.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.text.TextUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import fr.radiofrance.alarm.receiver.AlarmReceiver;
import fr.radiofrance.alarm.type.Day;
import fr.radiofrance.androidtoolbox.io.PrefsTools;
import fr.radiofrance.androidtoolbox.log.DebugLog;

/**
 * Created by mondon on 13/05/16.
 */
public class AlarmManager {

    private static final String TAG = AlarmManager.class.getSimpleName();
    private static final String KEY_ALARM_SNOOZE_DURATION = "AlarmSnoozeDuration";
    private static final String KEY_ALARM_DAYS = "AlarmDays";
    private static final String KEY_ALARM_HOURS = "AlarmHours";
    private static final String KEY_ALARM_MINUTES = "AlarmMinutes";
    private static final String KEY_ALARM_INTENT = "AlarmIntent";
    //    private static final String KEY_ALARM_RADIO_ID = "AlarmRadioId";
    private static final String KEY_ALARM_VOLUME = "AlarmVolume";
    private static final int DEFAULT_SNOOZE_DURATION = 600000;// 10 minutes

    private final Context context;
    private final AudioManager audioManager;
    private final MediaPlayer defaultAlarmSound;

    // Used to keep the system music volume to restore it when necessary
    private int systemVolume;

    public AlarmManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.defaultAlarmSound = MediaPlayer
                .create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        saveSystemVolume();
    }

    /**
     * Creates an alarm defined by alarmDays, hours and minutes.
     *
     * @param alarmIntent The intent that will be used for the alarm.
     * @param alarmDays   Days when the alarm will ring.
     * @param hours       The hours (from 0 to 23) when the alarm will ring.
     * @param minutes     The minutes (from 0 to 59) when the alarm will ring.
     */
    public long setAlarm(Intent alarmIntent, List<Day> alarmDays, int hours, int minutes) {
        if (hours < 0) hours = 0;
        if (minutes < 0) minutes = 0;

        Calendar nextAlarm = getNextAlarmDate(alarmDays, hours, minutes);
        if (nextAlarm == null) return -1;

        setAlarmDays(alarmDays);
        setAlarmHours(hours);
        setAlarmMinutes(minutes);
        setAlarmIntent(alarmIntent);

        long timeInMillis = nextAlarm.getTimeInMillis();
        scheduleAlarm(AlarmReceiver.ALARM_CODE, timeInMillis);

        return timeInMillis;
    }

    public Calendar getAlarm() {
        if (!isAlarmActivated()) return null;

        List<Day> days = getAlarmDays();
        int hours = getAlarmHours();
        int minutes = getAlarmMinutes();

        return getNextAlarmDate(days, hours, minutes);
    }

    public void snoozeAlarm() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.add(Calendar.MILLISECOND, getAlarmSnoozeDuration());

        scheduleAlarm(AlarmReceiver.SNOOZE_CODE, cal.getTimeInMillis());
    }

    public void cancelAlarm() {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.KEY_ALARM);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                AlarmReceiver.ALARM_CODE, intent, 0);
        alarmManager.cancel(sender);

        intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.KEY_SNOOZE);
        sender = PendingIntent.getBroadcast(context, AlarmReceiver.SNOOZE_CODE, intent, 0);
        alarmManager.cancel(sender);
    }

    public boolean isAlarmActivated() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.KEY_ALARM);

        return PendingIntent.getBroadcast(context, AlarmReceiver.ALARM_CODE, intent,
                PendingIntent.FLAG_NO_CREATE) != null;
    }

    public boolean playDefaultAlarmSound(int volume, boolean looping) {
        if (defaultAlarmSound == null) return false;

        saveSystemVolume();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, toValidVolume(volume), 0);
        defaultAlarmSound.setLooping(looping);
        defaultAlarmSound.seekTo(0);
        defaultAlarmSound.start();

        return true;
    }

    public boolean stopDefaultAlarmSound() {
        if (defaultAlarmSound == null) return false;

        defaultAlarmSound.pause();
        restoreSystemVolume();

        return true;
    }

    public void setAlarmVolume(int volume) {
        PrefsTools.setInteger(context, KEY_ALARM_VOLUME, volume);
    }

    public int getAlarmVolume() {
        return PrefsTools.getInteger(context, KEY_ALARM_VOLUME,
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    public void setSystemVolume(int volume) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, toValidVolume(volume), 0);
    }

    public int getSystemMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void setAlarmSnoozeDuration(int durationInMillis) {
        if (durationInMillis < 0) durationInMillis = DEFAULT_SNOOZE_DURATION;

        PrefsTools.setInteger(context, KEY_ALARM_SNOOZE_DURATION, durationInMillis);
    }

    public int getAlarmSnoozeDuration() {
        return PrefsTools.getInteger(context, KEY_ALARM_SNOOZE_DURATION, DEFAULT_SNOOZE_DURATION);
    }

    public void setAlarmDays(List<Day> alarmDays) {
        String alarmDaysString = "";

        if (alarmDays != null && alarmDays.size() > 0) {
            for (int i = 0; i < alarmDays.size(); ++i) {
                alarmDaysString += alarmDays.get(i).name();
                if (i < (alarmDays.size() - 1)) alarmDaysString += "-";
            }
        }

        PrefsTools.setString(context, KEY_ALARM_DAYS, alarmDaysString);
    }

    public List<Day> getAlarmDays() {
        String alarmDaysString = PrefsTools.getString(context, KEY_ALARM_DAYS, null);

        List<Day> days = new ArrayList<>();

        if (TextUtils.isEmpty(alarmDaysString)) return days;

        String[] daysString = alarmDaysString.split("-");
        for (String dayString : daysString) {
            try {
                days.add(Day.valueOf(dayString));
            } catch (IllegalArgumentException e) {
                DebugLog.d(TAG, "Error: Bad Day enum");
            }
        }

        return days;
    }

    public void setAlarmHours(int hours) {
        if (hours < 0) hours = 0;

        PrefsTools.setInteger(context, KEY_ALARM_HOURS, hours);
    }

    public int getAlarmHours() {
        return PrefsTools.getInteger(context, KEY_ALARM_HOURS, -1);
    }

    public void setAlarmMinutes(int minutes) {
        if (minutes < 0) minutes = 0;

        PrefsTools.setInteger(context, KEY_ALARM_MINUTES, minutes);
    }

    public int getAlarmMinutes() {
        return PrefsTools.getInteger(context, KEY_ALARM_MINUTES, -1);
    }

    public void setAlarmIntent(Intent alarmIntent) {
        if (alarmIntent == null) return;

        PrefsTools.setString(context, KEY_ALARM_INTENT, alarmIntent.toUri(0));
    }

    public Intent getAlarmIntent() {
        String intentUri = PrefsTools.getString(context, KEY_ALARM_INTENT, null);
        if (TextUtils.isEmpty(intentUri)) return null;

        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void scheduleAlarm(int alarmType, long timeInMillis) {
        android.app.AlarmManager am = (android.app.AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        if (alarmType == AlarmReceiver.SNOOZE_CODE) intent.setAction(AlarmReceiver.KEY_SNOOZE);
        else if (alarmType == AlarmReceiver.ALARM_CODE) intent.setAction(AlarmReceiver.KEY_ALARM);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmType, intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else am.set(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    private Calendar getNextAlarmDate(List<Day> alarmDays, int hours, int minutes) {
        if (alarmDays == null || alarmDays.size() <= 0) return null;
        if (hours < 0) hours = 0;
        if (minutes < 0) minutes = 0;

        boolean isNow = true;
        Calendar date = Calendar.getInstance(TimeZone.getDefault());

//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
//        Log.e("TEST", "Now=" + sdf.format(new Date(date.getTimeInMillis())));

        Calendar nextAlarmDate = null;
        while (nextAlarmDate == null) {
            if (alarmDays.contains(Day.getDayFromValue(date.get(Calendar.DAY_OF_WEEK)))
                    && (!isNow || isInFuture(date, hours, minutes))) {
                nextAlarmDate = getCalendar(date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH),
                        hours, minutes);
                continue;
            }

            isNow = false;
            date.add(Calendar.DATE, 1);
        }

//        Log.e("TEST", "Next alarm=" + sdf.format(new Date(nextAlarmDate.getTimeInMillis())));

        return nextAlarmDate;
    }

    private boolean isInFuture(Calendar date, int hours, int minutes) {
        if (date.get(Calendar.HOUR_OF_DAY) < hours) return true;
        else if (date.get(Calendar.HOUR_OF_DAY) == hours && date.get(Calendar.MINUTE) < minutes) {
            return true;
        }

        return false;
    }

    private Calendar getCalendar(int year, int month, int day, int hours, int minutes) {
        Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.set(year, month, day, hours, minutes, 0);
        return date;
    }

    private void saveSystemVolume() {
        systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void restoreSystemVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, toValidVolume(systemVolume), 0);
    }

    private int toValidVolume(int volume) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (volume < 0) volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        else if (volume > maxVolume) volume = maxVolume;

        return volume;
    }

}
