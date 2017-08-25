package fr.radiofrance.alarm;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AlarmManagerTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AlarmManager.initialize(context, new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com")), Alarm.class);
    }

    @Test
    public void addAlarm_isAlarmAdded() {
        Alarm alarm = new Alarm("18");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        alarm.setActivated(false);
        AlarmManager.addAlarm(context, alarm);

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
        AlarmManager.addAlarm(context, alarm);

        assertTrue(isAlarmActivated(alarm.getId()));
    }

    @Test
    public void removeAlarm_isAlarmRemoved() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm);

        assertTrue(isAlarmAdded(alarm.getId()));

        AlarmManager.removeAlarm(context, alarm.getId());

        assertNull(AlarmManager.getNextAlarm(context));
    }

    @Test
    public void addAlarms_areAlarmsAdded() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm);

        Alarm alarm2 = new Alarm("2");
        alarm2.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm2.setHours(15);
        alarm2.setMinutes(12);
        alarm2.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm2);

        Alarm alarm3 = new Alarm("3");
        alarm3.setDays(new ArrayList<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.SATURDAY)));
        alarm3.setHours(23);
        alarm3.setMinutes(4);
        alarm3.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm3);

        assertTrue(isAlarmAdded(alarm.getId()));
        assertTrue(isAlarmAdded(alarm2.getId()));
        assertTrue(isAlarmAdded(alarm3.getId()));
    }

    @Test
    public void removeAlarms_areAlarmsRemoved() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm);

        Alarm alarm2 = new Alarm("2");
        alarm2.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm2.setHours(15);
        alarm2.setMinutes(12);
        alarm2.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm2);

        Alarm alarm3 = new Alarm("3");
        alarm3.setDays(new ArrayList<>(Arrays.asList(Calendar.WEDNESDAY, Calendar.SATURDAY)));
        alarm3.setHours(23);
        alarm3.setMinutes(4);
        alarm3.setIntent(new Intent());
        AlarmManager.addAlarm(context, alarm3);

        assertTrue(isAlarmAdded(alarm.getId()));
        assertTrue(isAlarmAdded(alarm2.getId()));
        assertTrue(isAlarmAdded(alarm3.getId()));

        AlarmManager.removeAllAlarms(context);

        assertNull(AlarmManager.getNextAlarm(context));
    }

    @Test
    public void updateAlarm_isAlarmUpdated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm.setHours(13);
        alarm.setMinutes(16);
        alarm.setVolume(4);
        alarm.setSnoozeDuration(50);
        alarm.setIntent(new Intent("action"));
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertEquals(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)), updatedAlarm.getDays());
        assertEquals(13, updatedAlarm.getHours());
        assertEquals(16, updatedAlarm.getMinutes());
        assertEquals(4, updatedAlarm.getVolume());
        assertEquals(50, updatedAlarm.getSnoozeDuration());
        assertNotNull(updatedAlarm.getIntent());
        assertEquals("action", updatedAlarm.getIntent().getAction());
    }

    @Test
    public void updateAlarmDays_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertEquals(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)), updatedAlarm.getDays());
        assertTrue(isAlarmActivated(updatedAlarm.getId()));
    }

    @Test
    public void updateAlarmHours_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setHours(13);
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertEquals(13, updatedAlarm.getHours());
        assertTrue(isAlarmActivated(updatedAlarm.getId()));
    }

    @Test
    public void updateAlarmMinutes_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setMinutes(16);
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertEquals(16, updatedAlarm.getMinutes());
        assertTrue(isAlarmActivated(updatedAlarm.getId()));
    }

    @Test
    public void updateAlarmVolume_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setVolume(4);
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertEquals(4, updatedAlarm.getVolume());
        assertTrue(isAlarmActivated(updatedAlarm.getId()));
    }

    @Test
    public void updateAlarmSnoozeDuration_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setSnoozeDuration(50);
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertEquals(50, updatedAlarm.getSnoozeDuration());
        assertTrue(isAlarmActivated(updatedAlarm.getId()));
    }

    @Test
    public void updateAlarmIntent_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Update
        alarm.setIntent(new Intent("action"));
        AlarmManager.updateAlarm(context, alarm);

        // Get updated alarm
        Alarm updatedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(updatedAlarm);
        assertNotNull(updatedAlarm.getIntent());
        assertEquals("action", updatedAlarm.getIntent().getAction());
        assertTrue(isAlarmActivated(updatedAlarm.getId()));
    }

    @Test
    public void updateAlarmActivated_isAlarmActivated() {
        // Create
        Alarm alarm = getDefaultAlarm();
        AlarmManager.addAlarm(context, alarm);

        // Get added alarm
        Alarm addedAlarm = AlarmManager.getAlarm(context, alarm.getId());

        // Then
        assertNotNull(addedAlarm);
        assertTrue(isAlarmActivated(addedAlarm.getId()));
    }

    @After
    public void cleanAlarms() {
        AlarmManager.removeAllAlarms(context);
    }

    private Alarm getDefaultAlarm() {
        Alarm alarm = new Alarm("1");
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.MONDAY, Calendar.THURSDAY)));
        alarm.setHours(7);
        alarm.setMinutes(50);
        alarm.setVolume(2);
        alarm.setSnoozeDuration(10000);
        alarm.setIntent(new Intent());
        alarm.setActivated(true);
        return alarm;
    }

    private boolean isAlarmAdded(final String alarmId) {
        return AlarmManager.isAlarmAdded(context, alarmId);
    }

    private boolean isAlarmActivated(final String alarmId) {
        return AlarmManager.isAlarmScheduled(context, alarmId);
    }

}