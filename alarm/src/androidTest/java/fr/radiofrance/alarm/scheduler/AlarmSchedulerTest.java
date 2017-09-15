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
import fr.radiofrance.alarm.mock.DummyAlarmNotificationManager;
import fr.radiofrance.alarm.model.Alarm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AlarmSchedulerTest {

    private Context context;
    private AlarmScheduler alarmScheduler;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        alarmScheduler = new AlarmScheduler(context, new DummyAlarmNotificationManager(context), new ConfigurationDatastore(context));
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
        final Alarm alarm = getAlarmTest(context);

        // When
        alarmScheduler.scheduleAlarmSnooze(alarm);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmActived() {
        // Given
        final boolean actived = true;
        final Alarm alarm = getAlarmTest(context, actived);
        final List<Alarm> alarms = new ArrayList<>();
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
        final Alarm alarm = getAlarmTest(context, actived);
        final List<Alarm> alarms = new ArrayList<>();
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
        final Alarm alarm = getAlarmTest(context, actived);
        final List<Alarm> alarms = new ArrayList<>();
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
        final Alarm alarm_0h00 = getAlarmTest(context, true, 0, 0);
        final Alarm alarm_0h01 = getAlarmTest(context, true, 0, 1);
        final List<Alarm> alarms = new ArrayList<>();
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
        final Alarm alarm_0h01 = getAlarmTest(context, true, 0, 1);
        final Alarm alarm_0h00 = getAlarmTest(context, true, 0, 0);
        final List<Alarm> alarms = new ArrayList<>();
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
        final Alarm alarm_0h00 = getAlarmTest(context, true, 0, 0);
        final Alarm alarm_0h01 = getAlarmTest(context, true, 0, 1);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm_0h00);
        alarms.add(alarm_0h01);

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);
        alarmScheduler.unscheduleAlarmStandard(alarm_0h00);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
    }

    private static Alarm getAlarmTest(final Context context) {
        return getAlarmTest(context, true);
    }

    private static Alarm getAlarmTest(final Context context, boolean actived) {
        return getAlarmTest(new Intent(context, TestActivity.class), actived, 8, 45);
    }

    private static Alarm getAlarmTest(final Context context, boolean actived, int hours, int minutes) {
        return getAlarmTest(new Intent(context, TestActivity.class), actived, hours, minutes);
    }

    private static Alarm getAlarmTest(final Intent alarmIntent, boolean actived, int hours, int minutes) {
        final Alarm alarm = new Alarm();
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm.setHours(hours);
        alarm.setMinutes(minutes);
        alarm.setVolume(4);
        alarm.setActivated(actived);
        alarm.setSnoozeDuration(1000);
        alarm.setIntent(alarmIntent);
        return alarm;
    }

    public static class TestActivity extends Activity {}

}