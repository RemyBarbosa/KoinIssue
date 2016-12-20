package fr.radiofrance.alarm;

import android.content.Intent;
import android.media.AudioManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AlarmManagerTest {

    @Before
    public void setup() {
        AlarmManager.initialize(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AudioManager.STREAM_MUSIC, new Intent(), Alarm.class);
    }

    @Test
    public void addAlarm_isAlarmAdded() {
        Alarm alarm = new Alarm("18");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        alarm.setActivated(false);
        AlarmManager.getInstance().addAlarm(alarm);

        assertTrue(isAlarmAdded(alarm.getId()));
    }

    @Test
    public void addAlarm_isAlarmActivated() {
        Alarm alarm = new Alarm("10");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        alarm.setActivated(true);
        AlarmManager.getInstance().addAlarm(alarm);

        assertTrue(isAlarmActivated(alarm.getId()));
    }

    @Test
    public void removeAlarm_isAlarmRemoved() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm);

        assertTrue(isAlarmAdded(alarm.getId()));

        AlarmManager.getInstance().removeAlarm(alarm.getId());

        assertNull(AlarmManager.getInstance().getNextAlarm());
    }

    @Test
    public void add3Alarms_areAlarmsAdded() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm);

        Alarm alarm2 = new Alarm("2");
        alarm2.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm2.setHours(15);
        alarm2.setMinutes(12);
        alarm2.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm2);

        Alarm alarm3 = new Alarm("3");
        alarm3.setDays(new ArrayList<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.SATURDAY)));
        alarm3.setHours(23);
        alarm3.setMinutes(4);
        alarm3.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm3);

        assertTrue(isAlarmAdded(alarm.getId()));
        assertTrue(isAlarmAdded(alarm2.getId()));
        assertTrue(isAlarmAdded(alarm3.getId()));
    }

    @Test
    public void remove3Alarms_areAlarmsRemoved() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm);

        Alarm alarm2 = new Alarm("2");
        alarm2.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm2.setHours(15);
        alarm2.setMinutes(12);
        alarm2.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm2);

        Alarm alarm3 = new Alarm("3");
        alarm3.setDays(new ArrayList<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.SATURDAY)));
        alarm3.setHours(23);
        alarm3.setMinutes(4);
        alarm3.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm3);

        assertTrue(isAlarmAdded(alarm.getId()));
        assertTrue(isAlarmAdded(alarm2.getId()));
        assertTrue(isAlarmAdded(alarm3.getId()));

        AlarmManager.getInstance().removeAllAlarms();

        assertNull(AlarmManager.getInstance().getNextAlarm());
    }

    @Test
    public void updateAlarm_isAlarmUpdated() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.getInstance().addAlarm(alarm);

        assertTrue(isAlarmAdded(alarm.getId()));

        // Is alarm well saved
        Alarm savedAlarm = AlarmManager.getInstance().getAlarm(alarm.getId());

        assertEquals(alarm.getDays(), savedAlarm.getDays());
        assertEquals(alarm.getHours(), savedAlarm.getHours());
        assertEquals(alarm.getMinutes(), savedAlarm.getMinutes());

        Alarm updatedAlarm = new Alarm("1");
        updatedAlarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        updatedAlarm.setHours(13);
        updatedAlarm.setMinutes(16);
        updatedAlarm.setIntent(new Intent());
        AlarmManager.getInstance().updateAlarm(updatedAlarm);

        // Is alarm well updated
        savedAlarm = AlarmManager.getInstance().getAlarm(alarm.getId());

        assertNotEquals(alarm.getDays(), savedAlarm.getDays());
        assertNotEquals(alarm.getHours(), savedAlarm.getHours());
        assertNotEquals(alarm.getMinutes(), savedAlarm.getMinutes());
        assertEquals(updatedAlarm.getDays(), savedAlarm.getDays());
        assertEquals(updatedAlarm.getHours(), savedAlarm.getHours());
        assertEquals(updatedAlarm.getMinutes(), savedAlarm.getMinutes());
    }

    @After
    public void cleanAlarms() {
        AlarmManager.getInstance().removeAllAlarms();
    }

    private boolean isAlarmAdded(String alarmId) {
        return AlarmManager.getInstance().isAlarmAdded(alarmId);
    }

    private boolean isAlarmActivated(String alarmId) {
        return AlarmManager.getInstance().isAlarmScheduled(alarmId);
    }

}