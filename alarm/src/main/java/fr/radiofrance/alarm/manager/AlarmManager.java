package fr.radiofrance.alarm.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import fr.radiofrance.alarm.exception.AlarmException;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.receiver.AlarmReceiver;
import fr.radiofrance.alarm.util.PrefsUtils;

public class AlarmManager {

    public static final String INTENT_ALARM_ID = "IntentAlarmId";

    private static final String TAG = AlarmManager.class.getSimpleName();
    private static final String KEY_ALARMS = "AlarmsList";
    private static final String KEY_ALARM = "Alarm";
    private static final int DEFAULT_SNOOZE_DURATION = 600000;// 10 minutes

    private Context context;
    private int streamType = AudioManager.STREAM_ALARM;
    private Intent alarmClockIntent;
    private Class alarmItemClass;
    private AudioManager audioManager;
    private int deviceVolume;// Used to keep the device stream volume to restore it when necessary
    private MediaPlayer defaultAlarmSound;

    public static AlarmManager getInstance() {
        if (InstanceHolder.INSTANCE.context == null) {
            throw new AlarmException("You must first initialize AlarmManager");
        }

        return InstanceHolder.INSTANCE;
    }

    /**
     * Initializes AlarmManager and sets the audio stream type for this MediaPlayer.
     * See AudioManager for a list of stream types.
     *
     * @param context    The context
     * @param streamType The alarm stream type. See AudioManager for a list of stream types
     */
    public static void initialize(@NonNull Context context, int streamType, Intent alarmClockIntent, Class alarmItemClass) {
        AlarmManager instance = InstanceHolder.INSTANCE;

        instance.context = context;
        instance.streamType = streamType;
        instance.alarmClockIntent = alarmClockIntent;
        instance.alarmItemClass = alarmItemClass;
        instance.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        instance.deviceVolume = instance.audioManager.getStreamVolume(streamType);
    }

    /**
     * Adds an alarm.
     *
     * @param alarm Alarm that will ring.
     */
    public void addAlarm(@NonNull Alarm alarm) {
        int hours = alarm.getHours();
        int minutes = alarm.getMinutes();

        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            throw new AlarmException("Alarm time is incorrect");
        } else if (TextUtils.isEmpty(alarm.getId())) {
            throw new AlarmException("Alarm id is incorrect");
        } else if (alarm.getIntent() == null) {
            throw new AlarmException("Alarm intent is incorrect");
        }

        Calendar nextAlarmDate = getNextAlarmDate(alarm);

        if (alarm.getSnoozeDuration() < 0) {
            alarm.setSnoozeDuration(DEFAULT_SNOOZE_DURATION);
        }

        saveAlarm(alarm);

        if (alarm.isActivated()) {
            scheduleAlarm(AlarmReceiver.TYPE_ALARM, alarm.getId(), nextAlarmDate.getTimeInMillis());
        }
    }

    /**
     * Gets the Alarm previously added.
     *
     * @param alarmId The id of the Alarm to get
     * @return The Alarm
     */
    @SuppressWarnings("unchecked")
    public Alarm getAlarm(String alarmId) {
        if (TextUtils.isEmpty(alarmId) || !PrefsUtils.hasKey(context, KEY_ALARM + alarmId)) {
            return null;
        }

        String alarmString = PrefsUtils.getString(context, KEY_ALARM + alarmId);

        if (!TextUtils.isEmpty(alarmString)) {
            return (Alarm) new Gson().fromJson(alarmString, alarmItemClass);
        } else {
            return null;
        }
    }

    /**
     * Gets all alarms previously added.
     *
     * @return All alarms
     */
    @NonNull
    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();

        Set<String> alarmsIds = PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());
        for (String alarmId : alarmsIds) {
            Alarm alarm = getAlarm(alarmId);
            if (alarm != null) {
                alarms.add(alarm);
            }
        }

        return alarms;
    }

    /**
     * Gets all activated alarms previously added and activated.
     *
     * @return All alarms
     */
    public List<Alarm> getAllActivatedAlarms() {
        List<Alarm> alarms = new ArrayList<>();

        Set<String> alarmsIds = PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());
        for (String alarmId : alarmsIds) {
            Alarm alarm = getAlarm(alarmId);
            if (alarm != null && alarm.isActivated()) {
                alarms.add(alarm);
            }
        }

        return alarms;
    }

    /**
     * Gets all alarms ids previously added.
     *
     * @return All alarms ids
     */
    public Set<String> getAllAlarmsIds() {
        return PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());
    }

    /**
     * Updates an Alarm previously added.
     *
     * @param alarm The Alarm that contains the updates. This Alarm has to have the same id as the Alarm to update.
     */
    public void updateAlarm(@NonNull Alarm alarm) {
        String alarmId = alarm.getId();
        if (TextUtils.isEmpty(alarmId) || !PrefsUtils.hasKey(context, KEY_ALARM + alarmId)) {
            return;
        }

        Alarm savedAlarm = getAlarm(alarmId);
        if (savedAlarm == null) {
            return;
        }

        saveAlarm(alarm);
        boolean isAlarmActivated = alarm.isActivated();

        // If the days, hours and minutes have not been updated, we are only updating the new Alarm.
        if (!savedAlarm.getDays().equals(alarm.getDays())
                || savedAlarm.getHours() != alarm.getHours()
                || savedAlarm.getMinutes() != alarm.getMinutes()) {
            cancelAlarm(alarmId);

            if (isAlarmActivated) {
                Calendar nextAlarmDate = getNextAlarmDate(alarm);
                scheduleAlarm(AlarmReceiver.TYPE_ALARM, alarm.getId(), nextAlarmDate.getTimeInMillis());
            }
        } else if (!savedAlarm.isActivated() && isAlarmActivated) {
            Calendar nextAlarmDate = getNextAlarmDate(alarm);
            scheduleAlarm(AlarmReceiver.TYPE_ALARM, alarm.getId(), nextAlarmDate.getTimeInMillis());
        } else if (!isAlarmActivated) {
            cancelAlarm(alarmId);
        }
    }

    /**
     * Cancels an Alarm previously scheduled. So the Alarm will be deactivated.
     * You have to call {@link #updateAlarm(Alarm)} after calling {@link fr.radiofrance.alarm.model.Alarm#setActivated(boolean)} if you want to
     * activate it.
     *
     * @param alarmId The id of the Alarm to cancel
     */
    public void cancelAlarm(String alarmId) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Removing the normal alarm's pending intent from the real AlarmManager
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.KEY_ALARM + alarmId);
        PendingIntent sender = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_ALARM, intent, 0);
        alarmManager.cancel(sender);

        // Removing the snooze alarm's pending intent from the real AlarmManager
        intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.KEY_SNOOZE + alarmId);
        sender = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_SNOOZE, intent, 0);
        alarmManager.cancel(sender);

        // Removing the AlarmClock's pending intent from the real AlarmManager
        sender = PendingIntent.getActivity(context, 0, alarmClockIntent, 0);
        alarmManager.cancel(sender);
    }

    /**
     * Removes an Alarm previously added. So the Alarm will be deactivated.
     * You have to call {@link #addAlarm(Alarm)} again if you want to activate it after.
     *
     * @param alarmId The id of the Alarm to remove
     */
    public void removeAlarm(String alarmId) {
        if (TextUtils.isEmpty(alarmId)) return;

        // Canceling the alarm
        cancelAlarm(alarmId);

        // Removing the Alarm Id from the list of Alarms Ids
        Set<String> alarmsIds = PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());
        alarmsIds.remove(alarmId);
        PrefsUtils.setStringSet(context, KEY_ALARMS, alarmsIds);

        // Removing the Alarm
        PrefsUtils.removeKey(context, KEY_ALARM + alarmId);
    }

    /**
     * Removes all alarms previously added.
     * You have to call {@link #addAlarm(Alarm)} again if you want to activate them after.
     */
    public void removeAllAlarms() {
        Set<String> alarmsIds = PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());
        Object[] objects = alarmsIds.toArray();
        for (Object alarmId : objects) {
            removeAlarm((String) alarmId);
        }
    }

    /**
     * Snoozes an Alarm. The Alarm will ring in a specified frequency defined by {@link fr.radiofrance.alarm.model.Alarm#setSnoozeDuration(int)}.
     *
     * @param alarmId The id of the Alarm to snooze
     */
    public void snoozeAlarm(String alarmId) {
        if (TextUtils.isEmpty(alarmId)) return;

        Alarm alarm = getAlarm(alarmId);
        if (alarm == null || !alarm.isActivated()) return;

        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.add(Calendar.MILLISECOND, alarm.getSnoozeDuration());

        scheduleAlarm(AlarmReceiver.TYPE_SNOOZE, alarmId, cal.getTimeInMillis());
    }

    /**
     * Checks if an Alarm is added.
     *
     * @param alarmId The id of the Alarm to check
     * @return True if the Alarm is added, false otherwise
     */
    public boolean isAlarmAdded(String alarmId) {
        return !TextUtils.isEmpty(alarmId) && getAlarm(alarmId) != null;
    }

    /**
     * Checks if an Alarm is scheduled.
     *
     * @param alarmId The id of the Alarm to check
     * @return True if the Alarm is scheduled, false otherwise
     */
    public boolean isAlarmScheduled(String alarmId) {
        if (TextUtils.isEmpty(alarmId)) return false;

        Intent intentAlarm = new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.KEY_ALARM + alarmId);
        Intent intentSnooze = new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.KEY_SNOOZE + alarmId);

        return PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_ALARM, intentAlarm, PendingIntent.FLAG_NO_CREATE) != null
                || PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_SNOOZE, intentSnooze, PendingIntent.FLAG_NO_CREATE) != null;
    }

    /**
     * Sets the volume of the device stream.
     * The device stream defined by {@link #initialize(Context, int, Intent, Class)} will have this new volume.
     * To know the volume max authorized for this stream, please call {@link #getDeviceMaxVolume()}.
     *
     * @param volume The volume to set
     */
    public void setDeviceVolume(int volume) {
        audioManager.setStreamVolume(streamType, toValidVolume(volume), 0);
    }

    /**
     * Gets the volume of the device stream.
     * The device stream is defined by {@link #initialize(Context, int, Intent, Class)}.
     *
     * @return The volume to get
     */
    public int getDeviceVolume() {
        return audioManager.getStreamVolume(streamType);
    }

    /**
     * Gets the volume max of the device stream.
     * The device stream is defined by {@link #initialize(Context, int, Intent, Class)}.
     *
     * @return The volume to get
     */
    public int getDeviceMaxVolume() {
        return audioManager.getStreamMaxVolume(streamType);
    }

    /**
     * Gets the next Alarm that will ring from now.
     *
     * @return The next Alarm
     */
    public Alarm getNextAlarm() {
        Calendar nextDate = null;
        Alarm nextAlarm = null;
        Set<String> alarmsIds = PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());

        for (String alarmId : alarmsIds) {
            Alarm alarm = getAlarm(alarmId);
            if (alarm == null || !alarm.isActivated()) continue;

            Calendar date = getNextAlarmDate(alarm);
            if (nextDate == null || (date.before(nextDate))) {
                nextDate = date;
                nextAlarm = alarm;
            }
        }

        return nextAlarm;
    }

    /**
     * Gets the next Alarm date for all Alarms.
     *
     * @return The next Alarm date as Calendar
     */
    public Calendar getNextAlarmDate() {
        Alarm nextAlarm = getNextAlarm();
        if (nextAlarm != null) {
            return getNextAlarmDate(nextAlarm);
        }

        return null;
    }

    /**
     * Gets the next Alarm date for the specified Alarm.
     *
     * @param alarm The Alarm to get its next ring date
     * @return The next Alarm date
     */
    @NonNull
    public Calendar getNextAlarmDate(@NonNull Alarm alarm) {
        List<Integer> alarmDays = alarm.getDays();
        int hours = alarm.getHours();
        int minutes = alarm.getMinutes();

        // If no days are selected, we schedule a one shot alarm.
        if (alarmDays.isEmpty()) {
            alarmDays = Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY);
        }

        boolean isNow = true;
        Calendar date = Calendar.getInstance(TimeZone.getDefault());

        Calendar nextAlarmDate = null;
        while (nextAlarmDate == null) {
            if (alarmDays.contains(date.get(Calendar.DAY_OF_WEEK)) && (!isNow || isInFuture(date, hours, minutes))) {
                nextAlarmDate = getCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), hours, minutes);
                continue;
            }

            isNow = false;
            date.add(Calendar.DATE, 1);
        }

        return nextAlarmDate;
    }

    /**
     * Plays the default alarm sound.
     *
     * @param volume  The volume used for this sound
     * @param looping If you want to play this sound infinitely.
     * @return True if the sound is playing, false otherwise.
     */
    public boolean playDefaultAlarmSound(int volume, boolean looping) {
        if (isDefaultAlarmSoundPlaying()) return true;

        if (defaultAlarmSound == null) {
            defaultAlarmSound = new MediaPlayer();
            defaultAlarmSound.setAudioStreamType(streamType);
        }

        // Saving the current device stream volume so that this volume will be restored when the default alarm sound will be stopped.
        saveDeviceVolume();

        audioManager.setStreamVolume(streamType, toValidVolume(volume), 0);
        try {
            defaultAlarmSound.setDataSource(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
            defaultAlarmSound.setLooping(looping);
            defaultAlarmSound.prepare();
            defaultAlarmSound.start();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            defaultAlarmSound.release();
            defaultAlarmSound = null;

            return false;
        }
    }

    /**
     * Stops the default alarm sound started with {@link #playDefaultAlarmSound(int, boolean)}.
     */
    public void stopDefaultAlarmSound() {
        if (defaultAlarmSound == null) return;

        defaultAlarmSound.stop();
        defaultAlarmSound.release();
        defaultAlarmSound = null;
        restoreDeviceVolume();
    }

    public boolean isDefaultAlarmSoundPlaying() {
        return defaultAlarmSound != null && defaultAlarmSound.isPlaying();
    }

    /**
     * Schedules an Alarm in the real AlarmManager.
     *
     * @param alarmType    The type of the Alarm: AlarmReceiver.KEY_SNOOZE or AlarmReceiver.KEY_SNOOZE
     * @param alarmId      The if of the Alarm to schedule
     * @param timeInMillis The time in milliseconds of the Alarm
     * @return True if the Alarm has been scheduled, false otherwise
     */
    private boolean scheduleAlarm(int alarmType, String alarmId, long timeInMillis) {
        if (TextUtils.isEmpty(alarmId) || timeInMillis < 0) return false;

        // Creating Intent and PendingIntent for the AlarmReceiver.
        // We send the alarmId in the intent action because another Android dev could not retrieve it from the extras in an old project.
        Intent intent = new Intent(context, AlarmReceiver.class);
        if (alarmType == AlarmReceiver.TYPE_SNOOZE) {
            intent.setAction(AlarmReceiver.KEY_SNOOZE + alarmId);
        } else if (alarmType == AlarmReceiver.TYPE_ALARM) {
            intent.setAction(AlarmReceiver.KEY_ALARM + alarmId);
        } else {
            return false;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmType, intent, 0);

        // Setting the time in milliseconds to the AlarmManager.
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PendingIntent alarmPendingIntent = PendingIntent.getActivity(context, 0, alarmClockIntent, 0);
            am.setAlarmClock(new android.app.AlarmManager.AlarmClockInfo(timeInMillis, alarmPendingIntent), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            am.set(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        return true;
    }

    /**
     * Saves the Alarm in the SharedPreference.
     *
     * @param alarm The Alarm to save
     */
    private void saveAlarm(@NonNull Alarm alarm) {
        String alarmId = alarm.getId();

        // Updating the list of Alarms Ids
        Set<String> alarmsIds = PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>());
        alarmsIds.add(alarmId);
        PrefsUtils.setStringSet(context, KEY_ALARMS, alarmsIds);

        // Saving the Alarm
        PrefsUtils.setString(context, KEY_ALARM + alarmId, new Gson().toJson(alarm));
    }

    /**
     * Checks if the date given is in future.
     *
     * @param date    The date
     * @param hours   The hours of the date
     * @param minutes The minutes of the date
     * @return True if the date is in future, false otherwise
     */
    private boolean isInFuture(Calendar date, int hours, int minutes) {
        if (date.get(Calendar.HOUR_OF_DAY) < hours) {
            return true;
        } else if (date.get(Calendar.HOUR_OF_DAY) == hours && date.get(Calendar.MINUTE) < minutes) {
            return true;
        }

        return false;
    }

    /**
     * Gets the Calendar for the given parameters.
     *
     * @param year    The year
     * @param month   The month
     * @param day     The day
     * @param hours   The hours
     * @param minutes The minutes
     * @return The generated Calendar
     */
    private Calendar getCalendar(int year, int month, int day, int hours, int minutes) {
        Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.set(year, month, day, hours, minutes, 0);

        return date;
    }

    /**
     * Saves the current volume of the device stream.
     * The device stream is defined by {@link #initialize(Context, int, Intent, Class)}.
     */
    private void saveDeviceVolume() {
        deviceVolume = audioManager.getStreamVolume(streamType);
    }

    /**
     * Restores the volume of the device stream previously saved by {@link #saveDeviceVolume()}.
     */
    private void restoreDeviceVolume() {
        audioManager.setStreamVolume(streamType, toValidVolume(deviceVolume), 0);
    }

    /**
     * Returns the volume in a valid value.
     * If the volume is between 0 and {@link #getDeviceMaxVolume()}, the volume will not be modified.
     *
     * @param volume The volume
     * @return The new valid volume
     */
    private int toValidVolume(int volume) {
        int maxVolume = getDeviceMaxVolume();

        if (volume < 0) volume = audioManager.getStreamVolume(streamType);
        else if (volume > maxVolume) volume = maxVolume;

        return volume;
    }

    private static class InstanceHolder {

        private static final AlarmManager INSTANCE = new AlarmManager();

    }

}
