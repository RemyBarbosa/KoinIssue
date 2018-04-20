package fr.radiofrance.alarm.manager;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import fr.radiofrance.alarm.BuildConfig;
import fr.radiofrance.alarm.datastore.AlarmDatastore;
import fr.radiofrance.alarm.datastore.ConfigurationDatastore;
import fr.radiofrance.alarm.datastore.model.ScheduleData;
import fr.radiofrance.alarm.datastore.recovery.AlarmRecoveryModule;
import fr.radiofrance.alarm.exception.RfAlarmAlreadyExecutedException;
import fr.radiofrance.alarm.exception.RfAlarmDefaultLaunchIntentNotFoundException;
import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.notification.AlarmNotificationManager;
import fr.radiofrance.alarm.receiver.RfAlarmReceiver;
import fr.radiofrance.alarm.schedule.AlarmScheduler;
import fr.radiofrance.alarm.util.AlarmDateUtils;
import fr.radiofrance.alarm.util.AlarmIntentUtils;

public class RfAlarmManager {

    private static final String LOG_TAG = RfAlarmManager.class.getSimpleName();

    private static final int DEFAULT_SNOOZE_DURATION_MILLIS = 600000;// 10 minutes

    static volatile RfAlarmManager singleton = null;

    @NonNull
    private final Context context;
    @NonNull
    private final AlarmNotificationManager alarmNotificationManager;
    @NonNull
    private final AlarmScheduler alarmScheduler;
    @NonNull
    private final AlarmDatastore alarmDatastore;
    @NonNull
    private final ConfigurationDatastore configurationDatastore;

    private final boolean bootReceiverDisable;

    public static RfAlarmManager with(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (singleton == null) {
            synchronized (RfAlarmManager.class) {
                if (singleton == null) {
                    singleton = new Builder(context).build();
                }
            }
        }
        return singleton;
    }

    private RfAlarmManager(@NonNull final Context context, @NonNull final AlarmNotificationManager alarmNotificationManager, final boolean bootReceiverDisable) {
        this.context = context;
        this.configurationDatastore = new ConfigurationDatastore(context);
        this.alarmNotificationManager = alarmNotificationManager;
        this.alarmNotificationManager.setConfigurationDatastore(configurationDatastore);
        this.alarmScheduler = new AlarmScheduler(context, configurationDatastore, new AlarmScheduler.OnScheduleChangeListener() {
            @Override
            public void onChange(final ScheduleData standard, final ScheduleData snooze) {
                //Log.v(LOG_TAG, "AlarmScheduler.OnScheduleChangeListener - standard: " + (standard != null ? standard.alarmId : null) + " - snooze: " + (snooze != null ? snooze.alarmId : null));
                alarmNotificationManager.programNotification(standard, snooze);
            }
        });
        this.alarmDatastore = new AlarmDatastore(context);
        this.bootReceiverDisable = bootReceiverDisable;
    }

    /**
     * Intent use for Alarm with "intentUri" empty
     * or for recovery of Alarm build with previous version of library
     *
     * @param intent
     */
    public RfAlarmManager setConfigurationAlarmDefaultLaunchIntent(final Intent intent) {
        if (intent == null) {
            return this;
        }
        configurationDatastore.setAlarmDefaultLaunchIntent(intent);
        return this;
    }

    /**
     * Intent use to start app when AlarmLockscreen is restore from system current app
     *
     * @param intent
     */
    public RfAlarmManager setConfigurationAlarmAppLaunchIntent(final Intent intent) {
        if (intent == null) {
            return this;
        }
        configurationDatastore.setAlarmAppLaunchIntent(intent);
        return this;
    }

    /**
     * Intent use to start app alarm edit screen from system clock infos
     *
     * @param intent
     */
    public RfAlarmManager setConfigurationAlarmShowEditLaunchIntent(final Intent intent) {
        if (intent == null) {
            return this;
        }
        configurationDatastore.setAlarmShowEditLaunchIntent(intent);
        return this;
    }

    /**
     * Add recovery module for update alarm build on previous version of library
     *
     * @param recoveryModule
     */
    public RfAlarmManager setRecoveryModule(final AlarmRecoveryModule recoveryModule) {
        alarmDatastore.setRecoveryModule(recoveryModule);
        return this;
    }

    public void onDeviceReboot() {
        // The device has booted: we schedule all alarms that had been activated
        alarmScheduler.scheduleNextAlarmStandard(getAllAlarms());
    }

    @Nullable
    public Alarm getAlarm(final String alarmId) {
        if (TextUtils.isEmpty(alarmId)) {
            return null;
        }
        return alarmDatastore.getAlarm(alarmId);
    }

    @NonNull
    public List<Alarm> getAllAlarms() {
        return getAllAlarms(false, null);
    }

    @NonNull
    public List<Alarm> getAllAlarms(final boolean activeOnly) {
        return getAllAlarms(activeOnly, null);
    }

    @NonNull
    public List<Alarm> getAllAlarms(final Comparator<Alarm> sortComparator) {
        return getAllAlarms(false, sortComparator);
    }

    @NonNull
    public List<Alarm> getAllAlarms(final boolean activeOnly, final Comparator<Alarm> sortComparator) {
        final List<Alarm> alarms = new ArrayList<>();

        final Set<String> alarmIds = alarmDatastore.getAllAlarmIds();
        for (final String alarmId : alarmIds) {
            final Alarm alarm = alarmDatastore.getAlarm(alarmId);
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

    public void addAlarm(@Nullable final Alarm alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null");
            }
            alarm.setVersion(BuildConfig.LIBRARY_VERSION_CODE);

            checkAndSetAlarmParameters(alarm);

            if (!alarmDatastore.saveAlarm(alarm)) {
                throw new Exception("Error when saving alarm in datastore");
            }

            alarmScheduler.scheduleNextAlarmStandard(getAllAlarms());

            configureAlarmBootReceiver();

        } catch (Exception e) {
            throw new RfAlarmException("Error when adding alarm: " + e.getMessage(), e);
        }
    }

    public void updateAlarm(@Nullable final Alarm alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null");
            }
            if (alarmDatastore.getAlarm(alarm.getId()) == null) {
                throw new IllegalArgumentException("Alarm could not be update, no alarm found with id: " + alarm.getId());
            }
            checkAndSetAlarmParameters(alarm);

            if (!alarmDatastore.saveAlarm(alarm)) {
                throw new Exception("Error when saving alarm in datastore");
            }

            if (alarmNotificationManager.isLastNotificationShown(alarm.getId()) && !isNextAlarmUpcoming()) {
                alarmNotificationManager.hideNotification();
            }

            alarmScheduler.scheduleNextAlarmStandard(getAllAlarms());

            configureAlarmBootReceiver();

        } catch (Exception e) {
            throw new RfAlarmException("Error when updating alarm: " + e.getMessage(), e);
        }
    }

    public void removeAlarm(@Nullable final String alarmId) throws RfAlarmException {
        try {
            if (alarmId == null) {
                throw new IllegalArgumentException("AlarmId could not be null or empty");
            }
            final Alarm alarm = getAlarm(alarmId);
            if (alarm != null) {
                alarmScheduler.unscheduleAlarmStandard(alarm);
            }

            if (alarmNotificationManager.isLastNotificationShown(alarmId) && !isNextAlarmUpcoming()) {
                alarmNotificationManager.hideNotification();
            }

            if (!alarmDatastore.removeAlarm(alarmId)) {
                throw new Exception("Error when removing alarm from datastore");
            }

            configureAlarmBootReceiver();

        } catch (Exception e) {
            throw new RfAlarmException("Error when removing alarm: " + e.getMessage(), e);
        }
    }

    public void removeAllAlarms() throws RfAlarmException {
        final List<Alarm> allAlarms = getAllAlarms();
        for (final Alarm alarm : allAlarms) {
            if (alarm == null) {
                continue;
            }
            removeAlarm(alarm.getId());
        }
    }

    public Calendar getNextAlarmScheduleDate() {
        final Alarm nextAlarm = getNextAlarm();
        if (nextAlarm == null) {
            return null;
        }
        return AlarmDateUtils.getAlarmNextScheduleDate(nextAlarm);
    }

    public Alarm getNextAlarm() {
        Calendar nextDate = null;
        Alarm nextAlarm = null;

        final List<Alarm> allActiveAlarms = getAllAlarms(true);
        for (final Alarm alarm : allActiveAlarms) {
            final Calendar date = AlarmDateUtils.getAlarmNextScheduleDate(alarm);
            if (nextDate == null || (date.before(nextDate))) {
                nextDate = date;
                nextAlarm = alarm;
            }
        }
        return nextAlarm;
    }

    public boolean isAlarmSchedule(final Alarm alarm) {
        return alarmScheduler.isAlarmStandardSchedule(alarm);
    }

    public void onAlarmIsExecuted(final int alarmHash) throws RfAlarmAlreadyExecutedException {
        if (alarmHash != -1) {
            if (alarmHash == configurationDatastore.getAlarmLastExecutedHash()) {
                throw new RfAlarmAlreadyExecutedException(configurationDatastore.getAlarmAppLaunchIntent(null));
            }
            configurationDatastore.setAlarmLastExecutedHash(alarmHash);
        }
    }

    public void onAlarmIsConsumed(final Alarm alarm, final boolean isSnooze) throws RfAlarmException {
        try {
            if (!isSnooze && alarm.getDays().isEmpty()) {
                // If the alarm is a wake up and oneshot alarm, we deactivate it
                alarm.setActivated(false);
                updateAlarm(alarm);
            }
            if (!isNextAlarmUpcoming()) {
                alarmNotificationManager.hideNotification();
            }
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null.");
            }
            alarmScheduler.scheduleNextAlarmStandard(getAllAlarms());

            // Send broadcast for UI who need refresh
            context.sendBroadcast(AlarmIntentUtils.getOnAlarmNeedUiRefreshIntent(alarm.getId()));
        } catch (Exception e) {
            throw new RfAlarmException("Error on Alarm is consumed task: " + e.getMessage(), e);
        }
    }

    public void onAlarmIsSnoozed(final Alarm alarm) throws RfAlarmException {
        try {
            if (alarm == null) {
                throw new IllegalArgumentException("Alarm could not be null.");
            }
            alarmScheduler.scheduleAlarmSnooze(alarm);
            configureAlarmBootReceiver();
        } catch (Exception e) {
            throw new RfAlarmException("Error on Alarm is snooze task: " + e.getMessage(), e);
        }
    }

    public void onAlarmNotificationCancelBroadcastReceived(final String alarmId, final long alarmTimeMillis, final boolean isSnooze) throws RfAlarmException {
        try {
            if (TextUtils.isEmpty(alarmId)) {
                throw new IllegalArgumentException("Alarm id could not be null or empty.");
            }

            final Alarm alarm = getAlarm(alarmId);
            if (alarm != null) {
                if (!isSnooze && alarm.getDays().isEmpty()) {
                    alarm.setActivated(false);
                    alarm.setFromTimeMs(0L);
                } else {
                    alarm.setFromTimeMs(alarmTimeMillis + 1L);
                }
                alarmDatastore.saveAlarm(alarm);

                if (isSnooze) {
                    alarmScheduler.unscheduleAlarmSnooze(alarm);
                }
            }

            if (!isNextAlarmUpcoming(alarmId)) {
                alarmNotificationManager.hideNotification();
            }

            alarmScheduler.scheduleNextAlarmStandard(getAllAlarms());

            if (!isSnooze) {
                context.sendBroadcast(AlarmIntentUtils.getOnAlarmNeedUiRefreshIntent(alarmId));
            }
        } catch (Exception e) {
            throw new RfAlarmException("Error on Alarm notification is cancel task: " + e.getMessage(), e);
        }
    }

    public void onAlarmNotificationShowBroadcastReceived(final String alarmId, final long alarmTimeMillis, final boolean isSnooze) throws RfAlarmException {
        try {
            if (TextUtils.isEmpty(alarmId)) {
                throw new IllegalArgumentException("Alarm id could not be null or empty.");
            }
            alarmNotificationManager.showNotification(alarmId, alarmTimeMillis, isSnooze);
        } catch (Exception e) {
            throw new RfAlarmException("Error on Alarm notification should show task: " + e.getMessage(), e);
        }
    }

    public void onAlarmDeprecatedBroadcastReceived(final String alarmId) throws RfAlarmException {
        final Intent newLaunchIntent = configurationDatastore.getAlarmDefaultLaunchIntent(null);
        if (newLaunchIntent == null) {
            throw new RfAlarmDefaultLaunchIntentNotFoundException();
        }
        try {
            newLaunchIntent.putExtra(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID, alarmId);
            if (newLaunchIntent.getComponent() == null) {
                return;
            }
            final Class<?> act = Class.forName(newLaunchIntent.getComponent().getClassName());
            if (Activity.class.isAssignableFrom(act)) {
                newLaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(newLaunchIntent);
                return;
            }
            if (BroadcastReceiver.class.isAssignableFrom(act)) {
                context.sendBroadcast(newLaunchIntent);
                return;
            }
            if (Service.class.isAssignableFrom(act)) {
                context.startService(newLaunchIntent);
                return;
            }
        } catch (Exception e) {
            throw new RfAlarmException("Error on deprecated broadcast received: " + e.getMessage(), e);
        }
    }

    private Intent getConfigurationAlarmDefaultLaunchIntent() {
        return configurationDatastore.getAlarmDefaultLaunchIntent(null);
    }

    private void checkAndSetAlarmParameters(@NonNull final Alarm alarm) {
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
            alarm.setSnoozeDuration(DEFAULT_SNOOZE_DURATION_MILLIS);
        }
        if (alarm.getIntent() == null) {
            alarm.setIntent(getConfigurationAlarmDefaultLaunchIntent());
        }
        if (!alarm.isActivated()) {
            // Reset fromTimeMs because the last cancel action should be no more keep in memory
            alarm.setFromTimeMs(0L);
        }
    }

    private void configureAlarmBootReceiver() {
        if (bootReceiverDisable) {
            return;
        }
        if (alarmScheduler.hasAlarmScheluded()) {
            RfAlarmReceiver.enable(context);
            return;
        }
        RfAlarmReceiver.disable(context);
    }

    private boolean isNextAlarmUpcoming() {
        return isNextAlarmUpcoming(null);
    }

    private boolean isNextAlarmUpcoming(@Nullable final String filteredAlarmId) {
        final List<Alarm> alarms = getAllAlarms();
        for (final Alarm alarm : alarms) {
            if (alarm.getId().equals(filteredAlarmId)) {
                continue;
            }

            final long timeInMillis = AlarmDateUtils.getAlarmNextScheduleDate(alarm).getTimeInMillis();
            if ((alarm.isActivated() || alarmScheduler.isAlarmSnoozeSchedule(alarm)) && alarmNotificationManager.shouldShowNotificationNow(timeInMillis)) {
                return true;
            }
        }
        return false;
    }

    public static class Builder {

        private final Context context;
        private boolean bootReceiverDisable = false;
        private AlarmNotificationManager alarmNotificationManager;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
            this.alarmNotificationManager = new AlarmNotificationManager(context);
        }

        /**
         * Use for AndroidTestCase
         *
         * @param disabled
         * @return
         */
        public Builder bootReceiverDisable(final boolean disabled) {
            this.bootReceiverDisable = disabled;
            return this;
        }

        /**
         * Use for AndroidTestCase
         *
         * @param alarmNotificationManager
         * @return
         */
        public Builder setAlarmNotificationManager(final AlarmNotificationManager alarmNotificationManager) {
            this.alarmNotificationManager = alarmNotificationManager;
            return this;
        }

        public RfAlarmManager build() {
            return new RfAlarmManager(context, alarmNotificationManager, bootReceiverDisable);
        }

    }

}
