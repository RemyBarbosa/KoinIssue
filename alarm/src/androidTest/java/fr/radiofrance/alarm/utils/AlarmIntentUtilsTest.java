package fr.radiofrance.alarm.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
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

import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.AlarmIntentUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AlarmIntentUtilsTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void clear() {
    }

    @Test
    public void buildAlarmIntent_getExtras() {
        // Given
        final Alarm alarm = getAlarmTest(context);
        final boolean isSnooze = false;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze);

        // Then
        assertNotNull(alarmIntent);
        assertEquals(alarm.getIntent().getComponent(), alarmIntent.getComponent());
        assertNotEquals(-1, alarmIntent.getIntExtra(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_HASH, -1));
        assertEquals(alarm.getId(), alarmIntent.getStringExtra(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID));
        assertFalse(alarmIntent.getBooleanExtra(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_IS_SNOOZE, true));
    }

    @Test
    public void getPendingIntent_isPendingIntentAlive() {
        // Given
        final Alarm alarm = getAlarmTest(context);
        final boolean isSnooze = false;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze);
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarmIntent);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, alarmIntent));

        // Clean PendingIntents
        AlarmIntentUtils.cancelPendingIntent(context, alarmIntent);

    }


    @Test
    public void getPendingIntent_isPendingIntentNotAlive() {
        // Given
        final Alarm alarm = getAlarmTest(context);
        final boolean isSnooze = false;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze);

        // Then
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, alarmIntent));

    }

    @Test
    public void getOnePendingIntent_isOtherDifferentAlarmIntentNotAlive() {
        // Given
        final Alarm alarmA = getAlarmTest(new Intent(context, Activity.class));
        final Alarm alarmB = getAlarmTest(new Intent(context, Service.class));
        final boolean isSnooze = false;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarmA, isSnooze);
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarmIntent);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarmA, isSnooze)));
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarmB, isSnooze)));

        // Clean PendingIntents
        AlarmIntentUtils.cancelPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarmA, isSnooze));

    }

    @Test
    public void getOnePendingIntent_isOtherDifferentAlarmIntentIsAlsoAlive() {
        // Given
        final Alarm alarmA = getAlarmTest(new Intent(context, Activity.class));
        final Alarm alarmB = getAlarmTest(new Intent(context, Activity.class));
        final boolean isSnooze = false;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarmA, isSnooze);
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarmIntent);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarmA, isSnooze)));
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarmB, isSnooze)));

        // Clean PendingIntents
        AlarmIntentUtils.cancelPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarmA, isSnooze));

    }

    @Test
    public void getOnePendingIntent_isSameSnoozeAlarmIntentNotAlive() {
        // Given
        final Alarm alarm = getAlarmTest(new Intent(context, Activity.class));
        final boolean isSnooze = true;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze);
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarmIntent);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze)));
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarm, !isSnooze)));

        // Clean PendingIntents
        AlarmIntentUtils.cancelPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze));

    }

    @Test
    public void getOnePendingIntent_isSameNotSnoozeAlarmIntentNotAlive() {
        // Given
        final Alarm alarm = getAlarmTest(new Intent(context, Activity.class));
        final boolean isSnooze = false;

        // When
        final Intent alarmIntent = AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze);
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarmIntent);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze)));
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarm, !isSnooze)));

        // Clean PendingIntents
        AlarmIntentUtils.cancelPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze));

    }

    @Test
    public void cancelPendingIntent_isPendingIntentNotAlive() {
        // Given
        final Alarm alarm = getAlarmTest(context);
        final boolean isSnooze = false;

        // When
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze));
        AlarmIntentUtils.cancelPendingIntent(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze));

        // Then
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, AlarmIntentUtils.buildAlarmIntent(alarm, isSnooze)));

    }

    private static Alarm getAlarmTest(final Context context) {
        return getAlarmTest(new Intent(context, Activity.class));
    }

    private static Alarm getAlarmTest(final Intent alarmIntent) {
        final Alarm alarm = new Alarm();
        alarm.setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
        alarm.setHours(8);
        alarm.setMinutes(45);
        alarm.setVolume(4);
        alarm.setActivated(false);
        alarm.setSnoozeDuration(1000);
        alarm.setIntent(alarmIntent);
        return alarm;
    }

}