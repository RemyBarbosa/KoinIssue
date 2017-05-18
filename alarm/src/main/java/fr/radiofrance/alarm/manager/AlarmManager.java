package fr.radiofrance.alarm.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private static final String KEY_DEVICE_VOLUME = "alarm.manager.device.volume";
    private static final String KEY_SYSTEM_ALARM_CLOCK_INTENT = "alarm.manager.system.alarm.clock.intent";
    private static final String KEY_STREAM_TYPE = "alarm.manager.stream.type";
    private static final String KEY_ALARM_CLASS = "alarm.manager.alarm.class";
    private static final int DEFAULT_SNOOZE_DURATION = 600000;// 10 minutes

    public static void initialize(final Context context, final Intent systemAlarmClockIntent, final int streamType, final Class<? extends Alarm> alarmClass) {
        setSystemAlarmClockIntent(context, systemAlarmClockIntent);
        setStreamType(context, streamType);
        setAlarmClass(context, alarmClass);
    }

    /**
     * Adds an alarm.
     *
     * @param alarm Alarm that will ring
     * @return True if the alarm has been added, false otherwise
     */
    public static <T extends Alarm> boolean addAlarm(final Context context, @NonNull final T alarm) {
        try {
            final int hours = alarm.getHours();
            final int minutes = alarm.getMinutes();

            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                throw new AlarmException("Alarm time is incorrect");
            }
            if (TextUtils.isEmpty(alarm.getId())) {
                throw new AlarmException("Alarm id is incorrect");
            }
            if (alarm.getIntent() == null) {
                throw new AlarmException("Alarm intent is incorrect");
            }

            final Calendar nextAlarmDate = getNextAlarmDate(alarm);

            if (alarm.getSnoozeDuration() < 0) {
                alarm.setSnoozeDuration(DEFAULT_SNOOZE_DURATION);
            }

            saveAlarm(context, alarm);

            if (alarm.isActivated()) {
                scheduleAlarm(context, AlarmReceiver.TYPE_ALARM, alarm.getId(), nextAlarmDate.getTimeInMillis());
            }

            return true;
        } catch (AlarmException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the Alarm previously added.
     *
     * @param alarmId The id of the Alarm to get
     * @return The Alarm wanted
     */
    @Nullable
    public static <T extends Alarm> T getAlarm(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId) || !PrefsUtils.hasKey(context, KEY_ALARM + alarmId)) {
            return null;
        }

        final String alarmString = PrefsUtils.getString(context, KEY_ALARM + alarmId, null);

        if (!TextUtils.isEmpty(alarmString)) {
            final Class<Alarm> alarmClass = getAlarmClass(context);
            if (alarmClass == null) {
                return null;
            }
            return new Gson().<T>fromJson(alarmString, alarmClass);
        }

        return null;
    }

    /**
     * Gets all alarms previously added.
     *
     * @return All alarms
     */
    @NonNull
    public static <T extends Alarm> List<T> getAllAlarms(final Context context) {
        return getAllAlarms(context, null);
    }

    /**
     * Gets all alarms previously added and sorted by the given Comparator.
     *
     * @param sortComparator The Comparator to sort alarms
     * @return All alarms
     */
    @NonNull
    public static <T extends Alarm> List<T> getAllAlarms(final Context context, final Comparator<T> sortComparator) {
        final List<T> alarms = new ArrayList<>();

        final Set<String> alarmsIds = getAllAlarmsIds(context);
        for (final String alarmId : alarmsIds) {
            final T alarm = getAlarm(context, alarmId);
            if (alarm != null) {
                alarms.add(alarm);
            }
        }

        if (sortComparator != null) {
            Collections.sort(alarms, sortComparator);
        }

        return alarms;
    }

    /**
     * Gets all activated alarms previously added and activated.
     *
     * @return All alarms
     */
    @NonNull
    public static <T extends Alarm> List<T> getAllActivatedAlarms(final Context context) {
        return getAllActivatedAlarms(context, null);
    }

    /**
     * Gets all activated alarms previously added and activated and sorted by the given Comparator.
     *
     * @param sortComparator The Comparator to sort alarms
     * @return All alarms
     */
    @NonNull
    public static <T extends Alarm> List<T> getAllActivatedAlarms(final Context context, final Comparator<T> sortComparator) {
        final List<T> alarms = new ArrayList<>();

        final Set<String> alarmsIds = getAllAlarmsIds(context);
        for (final String alarmId : alarmsIds) {
            final T alarm = getAlarm(context, alarmId);
            if (alarm != null && alarm.isActivated()) {
                alarms.add(alarm);
            }
        }

        if (sortComparator != null) {
            Collections.sort(alarms, sortComparator);
        }

        return alarms;
    }

    /**
     * Gets all alarms ids previously added.
     *
     * @return All alarms ids
     */
    @NonNull
    public static Set<String> getAllAlarmsIds(final Context context) {
        return new HashSet<>(PrefsUtils.getStringSet(context, KEY_ALARMS, new HashSet<String>()));
    }

    public static void setSystemAlarmClockIntent(final Context context, final Intent systemAlarmClockIntent) {
        PrefsUtils.setString(context, KEY_SYSTEM_ALARM_CLOCK_INTENT, systemAlarmClockIntent.toUri(0));
    }

    @NonNull
    public static Intent getSystemAlarmClockIntent(final Context context) {
        final String intentUri = PrefsUtils.getString(context, KEY_SYSTEM_ALARM_CLOCK_INTENT, null);
        if (TextUtils.isEmpty(intentUri)) {
            throw new AlarmException("You must call AlarmManager.initialize(Context, Intent, int) first.");
        }

        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            throw new AlarmException("You must call AlarmManager.initialize(Context, Intent, int) first.");
        }
    }

    public static void setStreamType(final Context context, final int streamType) {
        PrefsUtils.setInteger(context, KEY_STREAM_TYPE, streamType);
    }

    public static int getStreamType(final Context context) {
        final int streamType = PrefsUtils.getInteger(context, KEY_STREAM_TYPE, -1);
        if (streamType == -1) {
            return AudioManager.STREAM_ALARM;
        }
        return streamType;
    }

    /**
     * Updates an Alarm previously added.
     *
     * @param alarm The Alarm that contains the updates. This Alarm has to have the same id as the Alarm to update
     * @return True if the Alarm has been updated (and maybe scheduled), false otherwise
     */
    public static <T extends Alarm> boolean updateAlarm(final Context context, @NonNull final T alarm) {
        final String alarmId = alarm.getId();
        if (TextUtils.isEmpty(alarmId) || !PrefsUtils.hasKey(context, KEY_ALARM + alarmId)) {
            return false;
        }

        final T savedAlarm = getAlarm(context, alarmId);
        if (savedAlarm == null) {
            return false;
        }

        saveAlarm(context, alarm);
        boolean isAlarmActivated = alarm.isActivated();

        // If the days, hours and minutes have not been updated, we are only updating the new Alarm.
        if (!savedAlarm.getDays().equals(alarm.getDays())
                || savedAlarm.getHours() != alarm.getHours()
                || savedAlarm.getMinutes() != alarm.getMinutes()) {
            cancelAlarm(context, alarmId);

            if (isAlarmActivated) {
                Calendar nextAlarmDate = getNextAlarmDate(alarm);
                scheduleAlarm(context, AlarmReceiver.TYPE_ALARM, alarmId, nextAlarmDate.getTimeInMillis());
            }
        } else if (isAlarmActivated && !savedAlarm.isActivated()) {
            Calendar nextAlarmDate = getNextAlarmDate(alarm);
            scheduleAlarm(context, AlarmReceiver.TYPE_ALARM, alarmId, nextAlarmDate.getTimeInMillis());
        } else if (isAlarmActivated && !alarm.getDays().isEmpty()) {
            Calendar nextAlarmDate = getNextAlarmDate(alarm);
            scheduleAlarm(context, AlarmReceiver.TYPE_ALARM, alarmId, nextAlarmDate.getTimeInMillis());
        } else if (!isAlarmActivated) {
            cancelAlarm(context, alarmId);
        } else if (!isAlarmScheduled(context, alarmId)) {
            Calendar nextAlarmDate = getNextAlarmDate(alarm);
            scheduleAlarm(context, AlarmReceiver.TYPE_ALARM, alarmId, nextAlarmDate.getTimeInMillis());
        }

        return true;
    }

    /**
     * Cancels an Alarm previously scheduled. So the Alarm will be deactivated.
     * You have to call {@link #updateAlarm(Context, Alarm)} after calling {@link fr.radiofrance.alarm.model.Alarm#setActivated(boolean)} if you want to
     * activate it.
     *
     * @param alarmId The id of the Alarm to cancel
     */
    public static void cancelAlarm(final Context context, final String alarmId) {
        final android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isAlarmNormalScheduled(context, alarmId)) {
            // Removing the normal alarm's pending intent from the real AlarmManager
            Intent intent = new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.KEY_ALARM + alarmId);
            PendingIntent sender = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_ALARM, intent, 0);
            alarmManager.cancel(sender);
        }

        if (isAlarmSnoozeScheduled(context, alarmId)) {
            // Removing the snooze alarm's pending intent from the real AlarmManager
            Intent intent = new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.KEY_SNOOZE + alarmId);
            PendingIntent sender = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_SNOOZE, intent, 0);
            alarmManager.cancel(sender);
        }

        // Removing the AlarmClock's pending intent from the real AlarmManager
        PendingIntent sender = PendingIntent.getActivity(context, 0, getSystemAlarmClockIntent(context), 0);
        alarmManager.cancel(sender);
    }

    /**
     * Removes an Alarm previously added. So the Alarm will be deactivated.
     * You have to call {@link #addAlarm(Context, Alarm)} again if you want to activate it after.
     *
     * @param alarmId The id of the Alarm to remove
     * @return True if the Alarm has been removed, false otherwise
     */
    public static boolean removeAlarm(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }

        // Canceling the alarm
        cancelAlarm(context, alarmId);

        // Removing the Alarm Id from the list of Alarms Ids
        final Set<String> alarmsIds = getAllAlarmsIds(context);
        alarmsIds.remove(alarmId);
        PrefsUtils.setStringSet(context, KEY_ALARMS, alarmsIds);

        // Removing the Alarm
        PrefsUtils.removeKey(context, KEY_ALARM + alarmId);

        return true;
    }

    /**
     * Removes all alarms previously added.
     * You have to call {@link #addAlarm(Context, Alarm)} again if you want to activate them after.
     *
     * @return True if all alarms have been removed, false if one or many have not
     */
    public static boolean removeAllAlarms(final Context context) {
        final Set<String> alarmsIds = getAllAlarmsIds(context);
        final Object[] objects = alarmsIds.toArray();
        boolean areAllAlarmsRemoved = true;
        for (final Object alarmId : objects) {
            if (!removeAlarm(context, (String) alarmId)) {
                areAllAlarmsRemoved = false;
            }
        }

        return areAllAlarmsRemoved;
    }

    /**
     * Snoozes an Alarm. The Alarm will ring in a specified frequency defined by {@link Alarm#setSnoozeDuration(int)}.
     *
     * @param alarmId The id of the Alarm to snooze
     * @return True if the Alarm has been snoozed, false otherwise
     */
    public static <T extends Alarm> boolean snoozeAlarm(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }

        final T alarm = getAlarm(context, alarmId);
        if (alarm == null) {
            return false;
        }

        final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.add(Calendar.MILLISECOND, alarm.getSnoozeDuration());

        scheduleAlarm(context, AlarmReceiver.TYPE_SNOOZE, alarmId, cal.getTimeInMillis());

        return true;
    }

    /**
     * Checks if an Alarm is added.
     *
     * @param alarmId The id of the Alarm to check
     * @return True if the Alarm is added, false otherwise
     */
    public static boolean isAlarmAdded(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }
        return getAlarm(context, alarmId) != null;
    }

    /**
     * Checks if an Alarm is scheduled.
     *
     * @param alarmId The id of the Alarm to check
     * @return True if the Alarm is scheduled, false otherwise
     */
    public static boolean isAlarmScheduled(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }
        return isAlarmNormalScheduled(context, alarmId) || isAlarmSnoozeScheduled(context, alarmId);
    }

    /**
     * Sets the volume of the device stream.
     * To know the volume max authorized for this stream, please call {@link #getDeviceMaxVolume(Context)}.
     *
     * @param volume The volume to set
     */
    public static void setDeviceVolume(final Context context, final int volume) {
        final int streamType = getStreamType(context);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(streamType, toValidVolume(context, volume), 0);
    }

    /**
     * Gets the volume of the device stream.
     *
     * @return The volume to get
     */
    public static int getDeviceVolume(final Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(getStreamType(context));
    }

    /**
     * Gets the volume max of the device stream.
     *
     * @return The volume to get
     */
    public static int getDeviceMaxVolume(final Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(getStreamType(context));
    }

    /**
     * Gets the next Alarm that will ring from now.
     *
     * @return The next Alarm
     */
    public static <T extends Alarm> T getNextAlarm(final Context context) {
        Calendar nextDate = null;
        T nextAlarm = null;

        final Set<String> alarmsIds = getAllAlarmsIds(context);
        for (final String alarmId : alarmsIds) {
            final T alarm = getAlarm(context, alarmId);
            if (alarm == null || !alarm.isActivated()) {
                continue;
            }

            final Calendar date = getNextAlarmDate(alarm);
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
    public static <T extends Alarm> Calendar getNextAlarmDate(final Context context) {
        final T nextAlarm = getNextAlarm(context);
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
    public static <T extends Alarm> Calendar getNextAlarmDate(@NonNull final T alarm) {
        final int hours = alarm.getHours();
        final int minutes = alarm.getMinutes();

        // If no days are selected, we schedule a one shot alarm.
        List<Integer> alarmDays = alarm.getDays();
        if (alarmDays.isEmpty()) {
            alarmDays = Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY);
        }

        final Calendar date = Calendar.getInstance(TimeZone.getDefault());

        boolean isNow = true;
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
    public static MediaPlayer playDefaultAlarmSound(final Context context, final int volume, final boolean looping) {
        return playAlarmSound(context, volume, looping, Settings.System.DEFAULT_ALARM_ALERT_URI);
    }

    /**
     * Plays the alarm sound specified by soundUri.
     *
     * @param volume   The volume used for this sound
     * @param looping  If you want to play this sound infinitely.
     * @param soundUri The Uri of the sound media.
     * @return True if the sound is playing, false otherwise.
     */
    public static MediaPlayer playAlarmSound(final Context context, final int volume, final boolean looping, final Uri soundUri) {
        final int streamType = getStreamType(context);

        final MediaPlayer defaultAlarmSound = new MediaPlayer();
        defaultAlarmSound.setAudioStreamType(streamType);

        // Saving the current device stream volume so that this volume will be restored when the default alarm sound will be stopped.
        saveDeviceVolume(context);

        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(streamType, toValidVolume(context, volume), 0);
        try {
            defaultAlarmSound.setDataSource(context, soundUri);
            defaultAlarmSound.setLooping(looping);
            defaultAlarmSound.prepare();
            defaultAlarmSound.start();

            return defaultAlarmSound;
        } catch (IOException e) {
            e.printStackTrace();
            defaultAlarmSound.release();

            return null;
        }
    }

    /**
     * Stops the default alarm sound started with {@link #playDefaultAlarmSound(Context, int, boolean)}
     * or with {@link #playAlarmSound(Context, int, boolean, Uri).
     */
    public static void stopDefaultAlarmSound(final Context context, final MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.stop();
        mediaPlayer.release();
        restoreDeviceVolume(context);
    }

    /**
     * Sets the Alarm class to set.
     *
     * @param alarmClass The class to set
     */
    private static void setAlarmClass(final Context context, final Class<?> alarmClass) {
        PrefsUtils.setString(context, KEY_ALARM_CLASS, alarmClass.getCanonicalName());
    }

    /**
     * Gets the Alarm class set in {@link #initialize(Context, Intent, int, Class)}.
     *
     * @return The class set
     */
    @SuppressWarnings("unchecked")
    private static <T extends Alarm> Class<T> getAlarmClass(final Context context) {
        try {
            return (Class<T>) Class.forName(PrefsUtils.getString(context, KEY_ALARM_CLASS, ""));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Checks if an Alarm normal (not snooze) is scheduled.
     *
     * @param alarmId The id of the Alarm to check
     * @return True if the Alarm is scheduled, false otherwise
     */
    private static boolean isAlarmNormalScheduled(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }

        final Intent intentAlarm = new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.KEY_ALARM + alarmId);
        return PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_ALARM, intentAlarm, PendingIntent.FLAG_NO_CREATE) != null;
    }

    /**
     * Checks if an Alarm snooze is scheduled.
     *
     * @param alarmId The id of the Alarm to check
     * @return True if the Alarm is scheduled, false otherwise
     */
    private static boolean isAlarmSnoozeScheduled(final Context context, final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return false;
        }

        final Intent intentSnooze = new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.KEY_SNOOZE + alarmId);
        return PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_SNOOZE, intentSnooze, PendingIntent.FLAG_NO_CREATE) != null;
    }

    /**
     * Schedules an Alarm in the real AlarmManager.
     *
     * @param alarmType    The type of the Alarm: AlarmReceiver.KEY_SNOOZE or AlarmReceiver.KEY_SNOOZE
     * @param alarmId      The if of the Alarm to schedule
     * @param timeInMillis The time in milliseconds of the Alarm
     * @return True if the Alarm has been scheduled, false otherwise
     */
    private static boolean scheduleAlarm(final Context context, final int alarmType, final String alarmId, final long timeInMillis) {
        if (TextUtils.isEmpty(alarmId) || timeInMillis < 0) {
            return false;
        }

        // Creating Intent and PendingIntent for the AlarmReceiver.
        // We send the alarmId in the intent action because another Android dev could not retrieve it from the extras in an old project.
        final Intent intent = new Intent(context, AlarmReceiver.class);
        if (alarmType == AlarmReceiver.TYPE_SNOOZE) {
            intent.setAction(AlarmReceiver.KEY_SNOOZE + alarmId);
        } else if (alarmType == AlarmReceiver.TYPE_ALARM) {
            intent.setAction(AlarmReceiver.KEY_ALARM + alarmId);
        } else {
            return false;
        }

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmType, intent, 0);

        // Setting the time in milliseconds to the AlarmManager.
        final android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PendingIntent alarmPendingIntent = PendingIntent.getActivity(context, 0, getSystemAlarmClockIntent(context), 0);
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
    private static <T extends Alarm> void saveAlarm(final Context context, @NonNull final T alarm) {
        final String alarmId = alarm.getId();

        // Updating the list of Alarms Ids
        final Set<String> alarmsIds = getAllAlarmsIds(context);
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
    private static boolean isInFuture(final Calendar date, final int hours, final int minutes) {
        if (date.get(Calendar.HOUR_OF_DAY) < hours) {
            return true;
        }
        if (date.get(Calendar.HOUR_OF_DAY) == hours && date.get(Calendar.MINUTE) < minutes) {
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
    private static Calendar getCalendar(final int year, final int month, final int day, final int hours, final int minutes) {
        final Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.set(year, month, day, hours, minutes, 0);

        return date;
    }

    /**
     * Saves the current volume of the device stream.
     */
    private static void saveDeviceVolume(final Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        PrefsUtils.setInteger(context, KEY_DEVICE_VOLUME, audioManager.getStreamVolume(getStreamType(context)));
    }

    /**
     * Restores the volume of the device stream previously saved by {@link #saveDeviceVolume(Context)}.
     */
    private static void restoreDeviceVolume(final Context context) {
        final int deviceVolume = PrefsUtils.getInteger(context, KEY_DEVICE_VOLUME, 0);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(getStreamType(context), toValidVolume(context, deviceVolume), 0);
    }

    /**
     * Returns the volume in a valid value.
     * If the volume is between 0 and {@link #getDeviceMaxVolume(Context)}, the volume will not be modified.
     *
     * @param volume The volume
     * @return The new valid volume
     */
    private static int toValidVolume(final Context context, int volume) {
        final int maxVolume = getDeviceMaxVolume(context);

        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (volume < 0) {
            volume = audioManager.getStreamVolume(getStreamType(context));
        } else if (volume > maxVolume) {
            volume = maxVolume;
        }

        return volume;
    }

}
