package fr.radiofrance.alarm.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import fr.radiofrance.alarm.BuildConfig;
import fr.radiofrance.alarm.datastore.AlarmDatastore;
import fr.radiofrance.alarm.datastore.ConfigurationDatastore;
import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.receiver.RfAlarmReceiver;
import fr.radiofrance.alarm.util.AlarmDateUtils;
import fr.radiofrance.alarm.util.AlarmIntentUtils;


public class RfAlarmManager<T extends Alarm> {

    private static final int DEFAULT_SNOOZE_DURATION = 600000;// 10 minutes

    @NonNull
    private final Context context;
    @NonNull
    private final AlarmManager alarmManager;
    @NonNull
    private final AlarmDatastore<T> alarmDatastore;
    @NonNull
    private final ConfigurationDatastore configurationDatastore;
    private final boolean bootReceiverDisable;

    public RfAlarmManager(@NonNull final Context context, @NonNull final Class<T> type) {
        this(context, type, false);
    }

    // Use for tests
    protected RfAlarmManager(@NonNull final Context context, @NonNull final Class<T> type, final boolean bootReceiverDisable) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.alarmDatastore = new AlarmDatastore<>(context, type);
        this.configurationDatastore = new ConfigurationDatastore(context);
        this.bootReceiverDisable = bootReceiverDisable;
    }

    /**
     * Intent use for Alarm with "intentUri" empty
     * or for recovery of Alarm build with previous version of library
     * @param intent
     */
    public void setConfigurationAlarmDefaultLaunchIntent(final Intent intent) {
        if (intent == null) {
            return;
        }
        configurationDatastore.setAlarmDefaultLaunchIntent(intent);
    }

    /**
     * Intent use to start app when AlarmLockscreen is restore from system current app
     * @param intent
     */
    public void setConfigurationAlarmAppLaunchIntent(final Intent intent) {
        if (intent == null) {
            return;
        }
        configurationDatastore.setAlarmAppLaunchIntent(intent);
    }

    /**
     * Intent use to start app alarm edit screen from system clock infos
     * @param intent
     */
    public void setConfigurationAlarmShowEditLaunchIntent(final Intent intent) {
        if (intent == null) {
            return;
        }
        configurationDatastore.setAlarmShowEditLaunchIntent(intent);
    }

    public Intent getConfigurationAlarmDefaultLaunchIntent() {
        return configurationDatastore.getAlarmDefaultLaunchIntent(null);
    }

    public void onDeviceReboot() {
        // The device has booted: we schedule all alarms that had been activated
        final List<T> alarms = getAllAlarms();
        for (final T alarm : alarms) {
            if (alarm.isActivated()) {
                scheduleAlarm(alarm, false);
            }
        }
    }

    @Nullable
    public T getAlarm(final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return null;
        }
        return alarmDatastore.getAlarm(alarmId);
    }

    @NonNull
    public List<T> getAllAlarms() {
        return getAllAlarms(false, null);
    }

    @NonNull
    public List<T> getAllAlarms(final boolean activeOnly) {
        return getAllAlarms(activeOnly, null);
    }

    @NonNull
    public List<T> getAllAlarms(final Comparator<T> sortComparator) {
        return getAllAlarms(false, sortComparator);
    }

    @NonNull
    public List<T> getAllAlarms(final boolean activeOnly, final Comparator<T> sortComparator) {
        final List<T> alarms = new ArrayList<>();

        final Set<String> alarmIds = alarmDatastore.getAllAlarmIds();
        for (final String alarmId : alarmIds) {
            final T alarm = alarmDatastore.getAlarm(alarmId);
            if (alarm == null) {
                continue;
            }
            if (!activeOnly) {
                alarms.add(alarm);
                continue;
            }
            if (alarm.isActivated()) {
                alarms.add(alarm);
            }
        }

        if (sortComparator != null) {
            Collections.sort(alarms, sortComparator);
        }

        return alarms;
    }

    public void addAlarm(@Nullable final T alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null");
            }
            alarm.setVersion(BuildConfig.LIBRARY_VERSION_CODE);

            checkAlarmValidity(alarm);

            if (!alarmDatastore.saveAlarm(alarm)) {
                throw new Exception("Error when saving alarm in datastore");
            }

            scheduleAlarm(alarm, false);

        } catch (Exception e) {
            throw new RfAlarmException("Error when adding alarm: " + e.getMessage(), e);
        }
    }


    public void updateAlarm(@Nullable final T alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null");
            }

            checkAlarmValidity(alarm);

            if (!alarmDatastore.saveAlarm(alarm)) {
                throw new Exception("Error when saving alarm in datastore");
            }

            cancelAlarm(alarm);

            scheduleAlarm(alarm, false);

        } catch (Exception e) {
            throw new RfAlarmException("Error when updating alarm: " + e.getMessage(), e);
        }
    }

    public void removeAlarm(@Nullable  final String alarmId) throws RfAlarmException {
        try {
            if (alarmId == null) {
                throw new IllegalArgumentException("AlarmId could not be null or empty");
            }
            final Alarm alarm = getAlarm(alarmId);
            if (alarm != null) {
                cancelAlarm(alarm);
            }

            if (!alarmDatastore.removeAlarm(alarmId)) {
                throw new Exception("Error when removing alarm from datastore");
            }

        } catch (Exception e) {
            throw new RfAlarmException("Error when removing alarm: " + e.getMessage(), e);
        }
    }

    public void removeAllAlarms() throws RfAlarmException {
        final List<T> allAlarms = getAllAlarms();
        for (final Alarm alarm : allAlarms) {
            if (alarm == null) {
                continue;
            }
            removeAlarm(alarm.getId());
        }
    }

    public Calendar getNextAlarmScheduleDate() {
        final T nextAlarm = getNextAlarm();
        if (nextAlarm == null) {
            return null;
        }
        return AlarmDateUtils.getAlarmNextScheduleDate(nextAlarm);
    }

    public T getNextAlarm() {
        Calendar nextDate = null;
        T nextAlarm = null;

        final List<T> allActiveAlarms = getAllAlarms(true);
        for (final T alarm : allActiveAlarms) {
            final Calendar date = AlarmDateUtils.getAlarmNextScheduleDate(alarm);
            if (nextDate == null || (date.before(nextDate))) {
                nextDate = date;
                nextAlarm = alarm;
            }
        }
        return nextAlarm;
    }

    public boolean isAlarmSchedule(final T alarm) {
        return AlarmIntentUtils.isPendingIntentAlive(context, alarm, false);
    }

    public void onAlarmIsConsumed(final T alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null.");
            }
            checkForRecovery(alarm);
            scheduleAlarm(alarm, false);
        } catch (Exception e) {
            throw new RfAlarmException("Error on Alarm is consumed task: " + e.getMessage(), e);
        }
    }

    public void onAlarmIsSnoozed(final T alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null.");
            }
            checkForRecovery(alarm);
            scheduleAlarm(alarm, false);
            scheduleAlarm(alarm, true);
        } catch (Exception e) {
            throw new RfAlarmException("Error on Alarm is snooze task: " + e.getMessage(), e);
        }
    }

    private void cancelAlarm(final Alarm alarm) {
        if (alarm == null) {
            return;
        }
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarm, false);
        if (pendingIntent == null) {
            return;
        }
        alarmManager.cancel(pendingIntent);
        AlarmIntentUtils.cancelPendingIntent(context, alarm, false);
    }

    private void checkAlarmValidity(@NonNull final T alarm) {
        final int hours = alarm.getHours();
        final int minutes = alarm.getMinutes();

        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Alarm time is incorrect");
        }
        if (TextUtils.isEmpty(alarm.getId())) {
            throw new IllegalArgumentException("Alarm id is incorrect");
        }
        if (alarm.getIntent() == null) {
            throw new IllegalArgumentException("Alarm intent is incorrect");
        }
        if (alarm.getSnoozeDuration() <= 0) {
            alarm.setSnoozeDuration(DEFAULT_SNOOZE_DURATION);
        }
        if (alarm.getIntent() == null) {
            alarm.setIntent(getConfigurationAlarmDefaultLaunchIntent());
        }
        checkForRecovery(alarm);
    }

    private void checkForRecovery(@NonNull final T alarm) {
        if (alarm.getVersion() < BuildConfig.LIBRARY_VERSION_CODE) {
            // Recovery action for alarm create on previous version of app
            alarm.setIntent(getConfigurationAlarmDefaultLaunchIntent());
            alarm.setVersion(BuildConfig.LIBRARY_VERSION_CODE);
        }
    }

    private void scheduleAlarm(final T alarm, final boolean isSnooze) {
        if (alarm == null) {
            return;
        }
        if (!alarm.isActivated()) {
            return;
        }

        Calendar scheduleDate;
        if (isSnooze) {
            scheduleDate = Calendar.getInstance(TimeZone.getDefault());
            scheduleDate.add(Calendar.MILLISECOND, alarm.getSnoozeDuration());

        } else {
            scheduleDate = AlarmDateUtils.getAlarmNextScheduleDate(alarm);
            if (scheduleDate.getTimeInMillis() < System.currentTimeMillis()) {
                return;
            }
        }

        final long timeInMillis = scheduleDate.getTimeInMillis();

        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarm, isSnooze);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PendingIntent alarmShowPendingIntent = AlarmIntentUtils.getActivityShowPendingIntent(context, getClockInfoShowEditIntent());
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(timeInMillis, alarmShowPendingIntent), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        configureAlarmBootReceiver();
    }

    private void configureAlarmBootReceiver() {
        if (bootReceiverDisable) {
            return;
        }
        for (final String alarmId : alarmDatastore.getAllAlarmIds()) {
            final T alarm = alarmDatastore.getAlarm(alarmId);
            if (alarm != null && alarm.isActivated()) {
                RfAlarmReceiver.enable(context);
                return;
            }
        }
        RfAlarmReceiver.disable(context);
    }

    private Intent getClockInfoShowEditIntent() {
        return configurationDatastore.getAlarmShowEditLaunchIntent(configurationDatastore.getAlarmAppLaunchIntent(null));
    }

}
