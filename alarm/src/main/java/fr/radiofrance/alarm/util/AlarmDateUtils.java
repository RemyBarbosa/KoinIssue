package fr.radiofrance.alarm.util;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import fr.radiofrance.alarm.model.Alarm;

public abstract class AlarmDateUtils {

    /**
     * Gets the next Alarm date for the specified Alarm from now.
     *
     * @param alarm The Alarm to get its next ring date
     * @return The next Alarm date
     */
    @NonNull
    public static Calendar getAlarmNextScheduleDate(@NonNull final Alarm alarm) {
        final long fromTimeInMillis = alarm.getFromTimeMs() > 0L ? alarm.getFromTimeMs() : System.currentTimeMillis();
        return getAlarmNextScheduleDateFrom(alarm, fromTimeInMillis);
    }

    /**
     * Gets the next Alarm date for the specified Alarm from specific time.
     *
     * @param alarm The Alarm to get its next ring date
     * @param fromTimeInMillis
     * @return The next Alarm date
     */
    @NonNull
    private static Calendar getAlarmNextScheduleDateFrom(@NonNull final Alarm alarm, final long fromTimeInMillis) {
        final int hours = alarm.getHours();
        final int minutes = alarm.getMinutes();

        // If no days are selected, we schedule a one shot alarm.
        List<Integer> alarmDays = alarm.getDays();
        if (alarmDays.isEmpty()) {
            alarmDays = Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY);
        }

        final Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.setTimeInMillis(fromTimeInMillis);

        boolean isNow = true;
        Calendar nextAlarmDate = null;
        while (nextAlarmDate == null) {
            if (alarmDays.contains(date.get(Calendar.DAY_OF_WEEK)) && (!isNow || isInFuture(date, hours, minutes))) {
                nextAlarmDate = getCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), hours, minutes);
                continue;
            }

            isNow = false;
            date.add(Calendar.DATE, 1);
        }

        return nextAlarmDate;
    }

    /**
     * Checks if the date given is in future.
     *
     * @param date    The date
     * @param hours   The hours of the date
     * @param minutes The minutes of the date
     * @return True if the date is in future, false otherwise
     */
    private static boolean isInFuture(final Calendar date, final int hours, final int minutes) {
        if (date.get(Calendar.HOUR_OF_DAY) < hours) {
            return true;
        }
        if (date.get(Calendar.HOUR_OF_DAY) == hours && date.get(Calendar.MINUTE) < minutes) {
            return true;
        }

        return false;
    }

    /**
     * Gets the Calendar for the given parameters.
     *
     * @param year    The year
     * @param month   The month
     * @param day     The day
     * @param hours   The hours
     * @param minutes The minutes
     * @return The generated Calendar
     */
    private static Calendar getCalendar(final int year, final int month, final int day, final int hours, final int minutes) {
        final Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.set(year, month, day, hours, minutes, 0);

        return date;
    }

}
