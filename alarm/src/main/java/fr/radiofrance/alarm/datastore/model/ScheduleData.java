package fr.radiofrance.alarm.datastore.model;

public class ScheduleData {

    public final String alarmId;
    public final long scheduleTimeMillis;

    public ScheduleData(final String alarmId, final long scheduleTimeMillis) {
        this.alarmId = alarmId;
        this.scheduleTimeMillis = scheduleTimeMillis;
    }
}
