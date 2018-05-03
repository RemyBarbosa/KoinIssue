package fr.radiofrance.alarm.domain

import java.util.*

data class AlarmModel(
        val id: String,
        var days: List<Int>,
        var hour: Int,
        var minute: Int,
        var enable: Boolean,
        var snoozeAtMillis: Long
) {
    fun nextStandarAlarmMillis(currentTimeMillis: Long): Long {
        if (!enable) return -1L
        days = days.sorted()

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = currentTimeMillis
        val hourOfNow = calendar.get(Calendar.HOUR_OF_DAY)
        val minuteOfNow = calendar.get(Calendar.MINUTE)
        val nowIsAfterAlarm = hour < hourOfNow || hour == hourOfNow && minute <= minuteOfNow
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        if (days.isEmpty()) {
            return if (nowIsAfterAlarm) {
                GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfMonth + 1, hour, minute, 0).timeInMillis
            } else {
                GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfMonth, hour, minute, 0).timeInMillis
            }
        } else {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val nextDayIndex = days.indexOfFirst { it > dayOfWeek }
            return if (days.contains(dayOfWeek)) {
                if (nowIsAfterAlarm) {
                    getNextTimeFromList(nextDayIndex, dayOfWeek, calendar, dayOfMonth)
                } else {
                    GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfMonth, hour, minute, 0).timeInMillis
                }
            } else {
                getNextTimeFromList(nextDayIndex, dayOfWeek, calendar, dayOfMonth)
            }
        }
    }

    fun nextAlarmMillis(currentTimeMillis: Long): Long {
        val alarmMillis = nextStandarAlarmMillis(currentTimeMillis)
        return if (snoozeAtMillis < currentTimeMillis) {
            alarmMillis
        } else {
            if (snoozeAtMillis < alarmMillis) snoozeAtMillis else alarmMillis
        }
    }

    private fun getNextTimeFromList(nextDayIndex: Int, dayOfWeek: Int, calendar: Calendar, dayOfMonth: Int): Long {
        return if (nextDayIndex != -1) {
            val dayInterval = days[nextDayIndex] - dayOfWeek
            GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfMonth + dayInterval, hour, minute, 0).timeInMillis
        } else {
            val dayInterval = Calendar.DAY_OF_WEEK - (dayOfWeek - days[0])
            GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfMonth + dayInterval, hour, minute, 0).timeInMillis
        }
    }
}