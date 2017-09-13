package fr.radiofrance.alarm.scheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import fr.radiofrance.alarm.datastore.ConfigurationDatastore;
import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;
import fr.radiofrance.alarm.model.Alarm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AlarmSchedulerTest {

    private Context context;
    private AlarmScheduler<AlarmTest> alarmScheduler;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        alarmScheduler = new AlarmScheduler<>(context, new ConfigurationDatastore(context));
    }

    @After
    public void clear() {
        new SharedPreferencesManager(context).flush();
    }

    @Test
    public void methods_nullParams() {
        alarmScheduler.scheduleNextAlarmStandard(null);
        alarmScheduler.scheduleAlarmSnooze(null);
        alarmScheduler.unscheduleAlarmStandard(null);

        assertFalse(alarmScheduler.isAlarmStandardSchedule(null));
    }

    @Test
    public void scheduleAlarmSnooze_isAlarmStandardReturnFalse() {
        // Given
        final AlarmTest alarm = new AlarmTest(context);

        // When
        alarmScheduler.scheduleAlarmSnooze(alarm);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmActived() {
        // Given
        final boolean actived = true;
        final AlarmTest alarm = new AlarmTest(context, actived);
        final List<AlarmTest> alarms = new ArrayList<>();
        alarms.add(alarm);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm));
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmUnactived() {
        // Given
        final boolean actived = false;
        final AlarmTest alarm = new AlarmTest(context, actived);
        final List<AlarmTest> alarms = new ArrayList<>();
        alarms.add(alarm);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmActivedUnscheduled() {
        // Given
        final boolean actived = true;
        final AlarmTest alarm = new AlarmTest(context, actived);
        final List<AlarmTest> alarms = new ArrayList<>();
        alarms.add(alarm);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);
        alarmScheduler.unscheduleAlarmStandard(alarm);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
    }

    @Test
    public void scheduleNextAlarmStandard_twoFollowingAlarmsInOrder() {
        // Given
        final AlarmTest alarm_0h00 = new AlarmTest(context, true, 0, 0);
        final AlarmTest alarm_0h01 = new AlarmTest(context, true, 0, 1);
        final List<AlarmTest> alarms = new ArrayList<>();
        alarms.add(alarm_0h00);
        alarms.add(alarm_0h01);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
    }

    @Test
    public void scheduleNextAlarmStandard_twoFollowingAlarmsNotInOrder() {
        // Given
        final AlarmTest alarm_0h01 = new AlarmTest(context, true, 0, 1);
        final AlarmTest alarm_0h00 = new AlarmTest(context, true, 0, 0);
        final List<AlarmTest> alarms = new ArrayList<>();
        alarms.add(alarm_0h01);
        alarms.add(alarm_0h00);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
    }

    public void scheduleNextAlarmStandard_twoFollowingAlarmsInOrderFirstOneUnschedule() {
        // Given
        final AlarmTest alarm_0h00 = new AlarmTest(context, true, 0, 0);
        final AlarmTest alarm_0h01 = new AlarmTest(context, true, 0, 1);
        final List<AlarmTest> alarms = new ArrayList<>();
        alarms.add(alarm_0h00);
        alarms.add(alarm_0h01);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);
        alarmScheduler.unscheduleAlarmStandard(alarm_0h00);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
    }


    private static class AlarmTest extends Alarm {

        AlarmTest(final Context context) {
            this(context, true);
        }

        AlarmTest(final Context context, boolean actived) {
            this(new Intent(context, Activity.class), actived, 8, 45);
        }

        AlarmTest(final Context context, boolean actived, int hours, int minutes) {
            this(new Intent(context, Activity.class), actived, hours, minutes);
        }

        AlarmTest(final Intent alarmIntent, boolean actived, int hours, int minutes) {
            setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
            setHours(hours);
            setMinutes(minutes);
            setVolume(4);
            setActivated(actived);
            setSnoozeDuration(1000);
            setIntent(alarmIntent);
        }

    }

}