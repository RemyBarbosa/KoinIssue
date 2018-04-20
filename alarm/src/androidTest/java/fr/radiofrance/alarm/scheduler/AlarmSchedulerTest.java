package fr.radiofrance.alarm.schedule;

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
import java.util.concurrent.CountDownLatch;

import fr.radiofrance.alarm.datastore.ConfigurationDatastore;
import fr.radiofrance.alarm.datastore.model.ScheduleData;
import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;
import fr.radiofrance.alarm.model.Alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AlarmSchedulerTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        new SharedPreferencesManager(context).flush();
    }

    @After
    public void clear() {
        new SharedPreferencesManager(context).flush();
    }

    @Test
    public void methods_nullParams() {
        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_NotExpected());
        alarmScheduler.scheduleNextAlarmStandard(null);
        alarmScheduler.scheduleAlarmSnooze(null);
        alarmScheduler.unscheduleAlarmStandard(null);

        assertFalse(alarmScheduler.isAlarmStandardSchedule(null));
        assertFalse(alarmScheduler.hasAlarmScheluded());
    }

    @Test
    public void scheduleAlarmSnooze_isAlarmStandardReturnFalse() {
        // Given
        final Alarm alarm = getAlarmTest(context);
        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_SnoozeExpected(alarm.getId()));

        // When
        alarmScheduler.scheduleAlarmSnooze(alarm);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
        assertTrue(alarmScheduler.hasAlarmScheluded());
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmActived() {
        // Given
        final boolean actived = true;
        final Alarm alarm = getAlarmTest(context, actived);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm);

        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_StandardExpected(alarm.getId()));

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm));
        assertTrue(alarmScheduler.hasAlarmScheluded());
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmUnactived() {
        // Given
        final boolean actived = false;
        final Alarm alarm = getAlarmTest(context, actived);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm);

        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_EmptyExpected());

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
        assertFalse(alarmScheduler.hasAlarmScheluded());
    }

    @Test
    public void scheduleNextAlarmStandard_oneAlarmActivedUnscheduled() {
        // Given
        final boolean actived = true;
        final Alarm alarm = getAlarmTest(context, actived);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm);

        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new AlarmScheduler.OnScheduleChangeListener() {
            boolean firstCall = true;
            @Override
            public void onChange(final ScheduleData standard, final ScheduleData snooze) {
                // Then
                if (firstCall) {
                    firstCall = false;
                    assertNull(snooze);
                    assertNotNull(standard);
                    assertEquals(alarm.getId(), standard.alarmId);
                    return;
                }
                assertNull(snooze);
                assertNull(standard);
            }
        });

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);
        alarmScheduler.unscheduleAlarmStandard(alarm);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm));
        assertFalse(alarmScheduler.hasAlarmScheluded());
    }

    @Test
    public void scheduleNextAlarmStandard_twoFollowingAlarmsInOrder() {
        // Given
        final Alarm alarm_0h00 = getAlarmTest(context, true, 0, 0);
        final Alarm alarm_0h01 = getAlarmTest(context, true, 0, 1);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm_0h00);
        alarms.add(alarm_0h01);

        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_StandardExpected(alarm_0h00.getId()));

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
        assertTrue(alarmScheduler.hasAlarmScheluded());
    }

    @Test
    public void scheduleNextAlarmStandard_twoFollowingAlarmsNotInOrder() {
        // Given
        final Alarm alarm_0h01 = getAlarmTest(context, true, 0, 1);
        final Alarm alarm_0h00 = getAlarmTest(context, true, 0, 0);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm_0h01);
        alarms.add(alarm_0h00);

        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_StandardExpected(alarm_0h00.getId()));

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);

        // Then
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
        assertTrue(alarmScheduler.hasAlarmScheluded());
    }

    public void scheduleNextAlarmStandard_twoFollowingAlarmsInOrderFirstOneUnschedule() {
        // Given
        final Alarm alarm_0h00 = getAlarmTest(context, true, 0, 0);
        final Alarm alarm_0h01 = getAlarmTest(context, true, 0, 1);
        final List<Alarm> alarms = new ArrayList<>();
        alarms.add(alarm_0h00);
        alarms.add(alarm_0h01);

        final AlarmScheduler alarmScheduler = new AlarmScheduler(context, new ConfigurationDatastore(context), new OnScheduleChangeListener_StandardExpected(alarm_0h00.getId(), alarm_0h01.getId()));

        // When
        alarmScheduler.scheduleNextAlarmStandard(alarms);
        alarmScheduler.unscheduleAlarmStandard(alarm_0h00);

        // Then
        assertFalse(alarmScheduler.isAlarmStandardSchedule(alarm_0h00));
        assertTrue(alarmScheduler.isAlarmStandardSchedule(alarm_0h01));
        assertTrue(alarmScheduler.hasAlarmScheluded());
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

    static class OnScheduleChangeListener_StandardExpected implements AlarmScheduler.OnScheduleChangeListener {
        private final CountDownLatch signal;
        private final String[] alarmIds;

        OnScheduleChangeListener_StandardExpected(final String... alarmIds) {
            this.alarmIds = alarmIds;
            this.signal = new CountDownLatch(alarmIds.length);
        }

        @Override
        public void onChange(final ScheduleData standard, final ScheduleData snooze) {
            // Then
            assertNull(snooze);
            assertNotNull(standard);
            assertEquals(alarmIds[(int)signal.getCount() - 1], standard.alarmId);
            signal.countDown();
        }
    }

    static class OnScheduleChangeListener_SnoozeExpected implements AlarmScheduler.OnScheduleChangeListener {
        private final CountDownLatch signal;
        private final String[] alarmIds;

        OnScheduleChangeListener_SnoozeExpected(final String... alarmIds) {
            this.alarmIds = alarmIds;
            this.signal = new CountDownLatch(alarmIds.length);
        }

        @Override
        public void onChange(final ScheduleData standard, final ScheduleData snooze) {
            // Then
            assertNull(standard);
            assertNotNull(snooze);
            assertEquals(alarmIds[(int)signal.getCount() - 1], snooze.alarmId);
            signal.countDown();
        }
    }

    static class OnScheduleChangeListener_EmptyExpected implements AlarmScheduler.OnScheduleChangeListener {
        private final CountDownLatch signal = new CountDownLatch(1);

        @Override
        public void onChange(final ScheduleData standard, final ScheduleData snooze) {
            // Then
            assertNull(standard);
            assertNull(snooze);
            signal.countDown();
        }
    }

    static class OnScheduleChangeListener_NotExpected implements AlarmScheduler.OnScheduleChangeListener {
        @Override
        public void onChange(final ScheduleData standard, final ScheduleData snooze) {
            // Then
            assertFalse("OnChange should not be fired", true);
        }

    }

}