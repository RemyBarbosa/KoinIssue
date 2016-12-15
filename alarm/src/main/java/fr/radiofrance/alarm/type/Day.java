package fr.radiofrance.alarm.type;

import java.util.Calendar;

public enum Day {

    MONDAY(Calendar.MONDAY), TUESDAY(Calendar.TUESDAY), WEDNESDAY(Calendar.WEDNESDAY),
    THURSDAY(Calendar.THURSDAY), FRIDAY(Calendar.FRIDAY), SATURDAY(Calendar.SATURDAY),
    SUNDAY(Calendar.SUNDAY);

    private final int value;

    Day(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Day getDayFromValue(int value) {
        for (Day day : values()) {
            if (day.getValue() == value) return day;
        }

        throw new IllegalArgumentException("Unknown value");
    }

}
