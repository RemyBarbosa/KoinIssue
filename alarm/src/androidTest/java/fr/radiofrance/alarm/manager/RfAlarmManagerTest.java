package fr.radiofrance.alarm.manager;

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

import fr.radiofrance.alarm.exception.RfAlarmException;
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
public class RfAlarmManagerTest {

    private Context context;
    private RfAlarmManager<AlarmTest> alarmManager;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        alarmManager = new RfAlarmManager<>(context, AlarmTest.class, true);
        try {
            alarmManager.removeAllAlarms();
        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }

    @After
    public void clear() {
        try {
            alarmManager.removeAllAlarms();
        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }

    @Test
    public void addAlarm_isAlarmAdded() {
        // Given
        final AlarmTest alarmToAdd = new AlarmTest();
        alarmToAdd.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarmToAdd.setHours(8);
        alarmToAdd.setMinutes(45);
        alarmToAdd.setVolume(4);
        alarmToAdd.setActivated(false);
        alarmToAdd.setSnoozeDuration(1000);
        alarmToAdd.setIntent(new Intent(context, Activity.class).setAction("Action"));

        try {
            // When
            alarmManager.addAlarm(alarmToAdd);

            // Then
            final AlarmTest alarm = alarmManager.getAlarm(alarmToAdd.getId());
            assertNotNull(alarm);
            assertFalse(alarm.isActivated());
            assertEquals(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)), alarm.getDays());
            assertEquals(8, alarm.getHours());
            assertEquals(45, alarm.getMinutes());
            assertEquals(4, alarm.getVolume());
            assertEquals(1000, alarm.getSnoozeDuration());
            assertFalse(alarm.isActivated());
            assertNotNull(alarm.getIntent());
            assertEquals("Action", alarm.getIntent().getAction());

            assertFalse(alarmManager.isAlarmSchedule(alarm));
            assertNull(alarmManager.getNextAlarm());
            assertNull(alarmManager.getNextAlarmScheduleDate());

        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }

    @Test
    public void addAlarmActived_isAlarmActivated() {
        // Given
        final AlarmTest alarmToAdd = new AlarmTest();
        alarmToAdd.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.THURSDAY)));
        alarmToAdd.setHours(7);
        alarmToAdd.setMinutes(50);
        alarmToAdd.setIntent(new Intent(context, Activity.class));
        alarmToAdd.setActivated(true);

        try {
            // When
            alarmManager.addAlarm(alarmToAdd);

            // Then
            final AlarmTest alarm = alarmManager.getAlarm(alarmToAdd.getId());
            assertNotNull(alarm);
            assertTrue(alarm.isActivated());
            assertTrue(alarmManager.isAlarmSchedule(alarm));
            assertNotNull(alarmManager.getNextAlarmScheduleDate());
            assertEquals(alarmToAdd.getId(), alarmManager.getNextAlarm().getId());

        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }

    @Test
    public void removeAlarm_isAlarmRemoved() {
        // Given
        final AlarmTest alarmToAdd = new AlarmTest();
        alarmToAdd.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.THURSDAY)));
        alarmToAdd.setHours(7);
        alarmToAdd.setMinutes(50);
        alarmToAdd.setIntent(new Intent(context, Activity.class));
        alarmToAdd.setActivated(true);

        try {
            // When
            alarmManager.addAlarm(alarmToAdd);
            alarmManager.removeAlarm(alarmToAdd.getId());

            // Then
            assertNull(alarmManager.getAlarm(alarmToAdd.getId()));
            assertFalse(alarmManager.isAlarmSchedule(alarmToAdd));
            assertNull(alarmManager.getNextAlarm());
            assertNull(alarmManager.getNextAlarmScheduleDate());

        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }

    @Test
    public void addAlarms_areAlarmsAdded() {
        // Given
        final AlarmTest alarm = new AlarmTest();
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setActivated(true);
        alarm.setIntent(new Intent(context, Activity.class));

        final AlarmTest alarm2 = new AlarmTest();
        alarm2.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm2.setHours(15);
        alarm2.setMinutes(12);
        alarm2.setActivated(true);
        alarm2.setIntent(new Intent(context, Activity.class));

        final AlarmTest alarm3 = new AlarmTest();
        alarm3.setDays(new ArrayList<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.SATURDAY)));
        alarm3.setHours(23);
        alarm3.setMinutes(4);
        alarm3.setActivated(true);
        alarm3.setIntent(new Intent(context, Activity.class));

        try {
            // When
            alarmManager.addAlarm(alarm);
            alarmManager.addAlarm(alarm2);
            alarmManager.addAlarm(alarm3);

            // Then
            assertNotNull(alarmManager.getAlarm(alarm.getId()));
            assertEquals(alarm.getHours(), alarmManager.getAlarm(alarm.getId()).getHours());

            assertNotNull(alarmManager.getAlarm(alarm2.getId()));
            assertEquals(alarm2.getHours(), alarmManager.getAlarm(alarm2.getId()).getHours());

            assertNotNull(alarmManager.getAlarm(alarm3.getId()));
            assertEquals(alarm3.getHours(), alarmManager.getAlarm(alarm3.getId()).getHours());

            assertNotNull(alarmManager.getNextAlarmScheduleDate());

        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }


    @Test
    public void removeAlarms_areAlarmsRemoved() {
        // Given
        final AlarmTest alarm = new AlarmTest();
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setActivated(true);
        alarm.setIntent(new Intent(context, Activity.class));

        final AlarmTest alarm2 = new AlarmTest();
        alarm2.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm2.setHours(15);
        alarm2.setMinutes(12);
        alarm2.setActivated(true);
        alarm2.setIntent(new Intent(context, Activity.class));

        final AlarmTest alarm3 = new AlarmTest();
        alarm3.setDays(new ArrayList<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.SATURDAY)));
        alarm3.setHours(23);
        alarm3.setMinutes(4);
        alarm3.setActivated(true);
        alarm3.setIntent(new Intent(context, Activity.class));

        try {
            // When
            alarmManager.addAlarm(alarm);
            alarmManager.addAlarm(alarm2);
            alarmManager.addAlarm(alarm3);

            alarmManager.removeAllAlarms();

            // Then
            assertNull(alarmManager.getAlarm(alarm.getId()));
            assertNull(alarmManager.getAlarm(alarm2.getId()));
            assertNull(alarmManager.getAlarm(alarm3.getId()));

            assertNull(alarmManager.getNextAlarmScheduleDate());

        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }


    @Test
    public void updateAlarm_isAlarmUpdated() {
        // Given
        final AlarmTest alarm = new AlarmTest();
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setVolume(3);
        alarm.setSnoozeDuration(100);
        alarm.setActivated(true);
        alarm.setIntent(new Intent(context, Activity.class).setAction("ActionOne"));

        try {
            // When
            alarmManager.addAlarm(alarm);
            alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
            alarm.setHours(8);
            alarm.setMinutes(45);
            alarm.setVolume(4);
            alarm.setActivated(false);
            alarm.setSnoozeDuration(1000);
            alarm.setIntent(new Intent(context, Activity.class).setAction("ActionTwo"));
            alarmManager.updateAlarm(alarm);

            // Then
            AlarmTest updatedAlarm = alarmManager.getAlarm(alarm.getId());

            // Then
            assertNotNull(updatedAlarm);
            assertEquals(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)), updatedAlarm.getDays());
            assertEquals(8, updatedAlarm.getHours());
            assertEquals(45, updatedAlarm.getMinutes());
            assertEquals(4, updatedAlarm.getVolume());
            assertEquals(1000, updatedAlarm.getSnoozeDuration());
            assertFalse(alarmManager.isAlarmSchedule(updatedAlarm));
            assertFalse(updatedAlarm.isActivated());
            assertNotNull(updatedAlarm.getIntent());
            assertEquals("ActionTwo", updatedAlarm.getIntent().getAction());

        } catch (RfAlarmException e) {
            assertTrue("Exception: " + e.getMessage(), false);
        }
    }

    private static class AlarmTest extends Alarm {
        public final String value;

        public AlarmTest() {
            this(null);
        }

        public AlarmTest(final String value) {
            super();
            this.value = value;
        }
    }

}