package fr.radiofrance.alarm.domain

import fr.radiofrance.alarm.data.datasource.room.AlarmEntity
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest
import java.util.*

@RunWith(Parameterized::class)
class AlarmModelTestParameterized(val hourOfNow: Int,
                                  val minuteOfNow: Int,
                                  val hourOfAlarm: Int,
                                  val minuteOfAlarm: Int,
                                  val days: List<Int>,
                                  val dayOfMonth: Int,
                                  val expectedDayOfMonth: Int
) : KoinTest {
    companion object {
        const val YEAR = 2018
        const val MONTH = Calendar.MARCH
        const val DAY_OF_MONTH_WEDNESDAY = 14

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: now : {0}h{1} alarm {2}h{3} days : {4}  dayOfMonth : {5} expectedDayOfMonth : {6}")
        fun data(): Collection<Array<Any>> {
            return Arrays.asList(
                    arrayOf(20, 0, 19, 30, listOf<Int>(), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 1),
                    arrayOf(19, 30, 19, 30, listOf<Int>(), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 1),
                    arrayOf(19, 0, 19, 30, listOf<Int>(), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY),

                    arrayOf(20, 0, 19, 30, listOf(Calendar.MONDAY, Calendar.WEDNESDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 5),
                    arrayOf(20, 0, 19, 30, listOf(Calendar.WEDNESDAY, Calendar.FRIDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 2),
                    arrayOf(19, 0, 19, 30, listOf(Calendar.MONDAY, Calendar.WEDNESDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY),

                    arrayOf(20, 0, 19, 30, listOf(Calendar.FRIDAY, Calendar.MONDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 2),
                    arrayOf(19, 0, 19, 30, listOf(Calendar.MONDAY, Calendar.FRIDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 2),
                    arrayOf(20, 0, 19, 30, listOf(Calendar.TUESDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 6),
                    arrayOf(19, 0, 19, 30, listOf(Calendar.TUESDAY, Calendar.MONDAY), DAY_OF_MONTH_WEDNESDAY, DAY_OF_MONTH_WEDNESDAY + 5)
            )
        }
    }

    @Test
    fun testIsAlarmIsEnable() {
        // when
        val now = GregorianCalendar(YEAR, MONTH, dayOfMonth, hourOfNow, minuteOfNow, 0)
        val alarm = AlarmMapper.modelFrom(AlarmEntity(days = days, hour = hourOfAlarm, minute = minuteOfAlarm))

        // then
        Assert.assertEquals(
                alarm.nextStandarAlarmMillis(now.timeInMillis),
                GregorianCalendar(YEAR,
                        MONTH,
                        expectedDayOfMonth,
                        alarm.hour,
                        alarm.minute,
                        0).timeInMillis
        )
    }

}