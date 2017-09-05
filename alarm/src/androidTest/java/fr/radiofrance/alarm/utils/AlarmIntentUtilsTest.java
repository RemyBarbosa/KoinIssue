package fr.radiofrance.alarm.utils;

import android.app.Activity;
import android.app.PendingIntent;
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

import static org.junit.Assert.assertFalse;
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
    public void getPendingIntent_isPendingIntentAlive() {
        // Given
        final AlarmTest alarm = new AlarmTest(context);
        final boolean isSnooze = false;

        // When
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarm, isSnooze);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, alarm, isSnooze));

    }

    @Test
    public void getPendingIntent_isPendingIntentNotAlive() {
        // Given
        final AlarmTest alarm = new AlarmTest(context);
        final boolean isSnooze = false;

        // When
        // nothing

        // Then
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, alarm, isSnooze));

    }

    @Test
    public void getOnePendingIntent_isOtherPendingIntentNotAlive() {
        // Given
        final AlarmTest alarmA = new AlarmTest(context);
        final AlarmTest alarmB = new AlarmTest(context);
        final boolean isSnooze = false;

        // When
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarmA, isSnooze);

        // Then
        assertNotNull(pendingIntent);
        assertTrue(AlarmIntentUtils.isPendingIntentAlive(context, alarmA, isSnooze));
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, alarmB, isSnooze));

    }

    @Test
    public void cancelPendingIntent_isPendingIntentNotAlive() {
        // Given
        final AlarmTest alarm = new AlarmTest(context);
        final boolean isSnooze = false;

        // When
        final PendingIntent pendingIntent = AlarmIntentUtils.getPendingIntent(context, alarm, isSnooze);
        AlarmIntentUtils.cancelPendingIntent(context, alarm, isSnooze);

        // Then
        assertFalse(AlarmIntentUtils.isPendingIntentAlive(context, alarm, isSnooze));

    }

    private static class AlarmTest extends Alarm {

        AlarmTest(final Context context) {
            setDays(new ArrayList<>(Arrays.asList(Calendar.TUESDAY, Calendar.FRIDAY)));
            setHours(8);
            setMinutes(45);
            setVolume(4);
            setActivated(false);
            setSnoozeDuration(1000);
            setIntent(new Intent(context, Activity.class).setAction("ActionTwo"));
        }

    }

}