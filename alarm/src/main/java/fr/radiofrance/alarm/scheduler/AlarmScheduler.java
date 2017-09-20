package fr.radiofrance.alarm.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import fr.radiofrance.alarm.datastore.ConfigurationDatastore;
import fr.radiofrance.alarm.datastore.SchedulerDatastore;
import fr.radiofrance.alarm.datastore.model.ScheduleData;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.AlarmDateUtils;
import fr.radiofrance.alarm.util.AlarmIntentUtils;

public class AlarmScheduler {

    public interface OnScheduleChangeListener {
        void onChange(ScheduleData standard, ScheduleData snooze);
    }

    @NonNull
    private final Context context;
    @NonNull
    private final ConfigurationDatastore configurationDatastore;
    @NonNull
    private final SchedulerDatastore schedulerDatastore;
    @NonNull
    private final AlarmManager alarmManager;

    private final OnScheduleChangeListener listener;

    public AlarmScheduler(@NonNull final Context context, @NonNull final ConfigurationDatastore configurationDatastore, final OnScheduleChangeListener listener) {
        this.context = context;
        this.configurationDatastore = configurationDatastore;
        this.schedulerDatastore = new SchedulerDatastore(context);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.listener = listener;
    }

    public void scheduleNextAlarmStandard(final List<Alarm> alarms) {
        if (alarms == null || alarms.isEmpty()) {
            return;
        }

        final ScheduleData currentScheduleData = schedulerDatastore.getCurrentStandard();
        final String currentStandardScheduledAlarmId = currentScheduleData != null ? currentScheduleData.alarmId : null;

        Calendar nextDate = null;
        Alarm nextAlarm = null;

        for (final Alarm alarm : alarms) {
            if (alarm.getId().equals(currentStandardScheduledAlarmId)) {
                unscheduleAlarmStandard(alarm);
            }
            if (!alarm.isActivated()) {
                continue;
            }

            final Calendar date = AlarmDateUtils.getAlarmNextScheduleDate(alarm);
            if (nextDate == null || (date.before(nextDate))) {
                nextDate = date;
                nextAlarm = alarm;
            }
        }

        if (nextAlarm != null) {
            scheduleAlarm(nextAlarm, false);
        }
    }

    public void scheduleAlarmSnooze(final Alarm alarm) {
        scheduleAlarm(alarm, true);
    }

    public boolean isAlarmStandardSchedule(final Alarm alarm) {
        if (alarm == null) {
            return false;
        }
        final ScheduleData currentScheduleData = schedulerDatastore.getCurrentStandard();
        if (currentScheduleData == null) {
            return false;
        }
        final boolean isCurrentScheduled = alarm.getId().equals(currentScheduleData.alarmId);
        return isCurrentScheduled && AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarm, false));
    }

    public void unscheduleAlarmStandard(final Alarm alarm) {
        if (alarm == null) {
            return;
        }
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, false));
        if (pendingIntent == null) {
            return;
        }
        alarmManager.cancel(pendingIntent);
        AlarmIntentUtils.cancelPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, false));
        schedulerDatastore.saveCurrentStandard(null);

        if (listener != null) {
            listener.onChange(schedulerDatastore.getCurrentStandard(), schedulerDatastore.getCurrentSnooze());
        }
    }

    private void scheduleAlarm(final Alarm alarm, final boolean isSnooze) {
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

        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PendingIntent alarmShowPendingIntent = AlarmIntentUtils.getActivityShowPendingIntent(context, getClockInfoShowEditIntent());
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(timeInMillis, alarmShowPendingIntent), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        if (isSnooze) {
            schedulerDatastore.saveCurrentSnooze(new ScheduleData(alarm.getId(), timeInMillis));
        } else {
            schedulerDatastore.saveCurrentStandard(new ScheduleData(alarm.getId(), timeInMillis));
        }
        if (listener != null) {
            listener.onChange(schedulerDatastore.getCurrentStandard(), schedulerDatastore.getCurrentSnooze());
        }
    }

    private Intent getClockInfoShowEditIntent() {
        return configurationDatastore.getAlarmShowEditLaunchIntent(configurationDatastore.getAlarmAppLaunchIntent(new Intent(Intent.ACTION_VIEW)));
    }

}
