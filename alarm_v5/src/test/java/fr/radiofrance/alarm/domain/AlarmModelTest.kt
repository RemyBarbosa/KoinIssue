package fr.radiofrance.alarm.domain

import fr.radiofrance.alarm.data.datasource.room.AlarmEntity
import junit.framework.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.util.*

class AlarmModelTest : KoinTest {
    @Test
    fun testIsAlarmDisable_negativeTimeMillis() {
        // when
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val now = GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 19, 35, 0).timeInMillis
        val alarm = AlarmMapper.modelFrom(AlarmEntity(hour = 19, minute = 34, enable = false))

        // then
        Assert.assertEquals(alarm.nextStandarAlarmMillis(now), -1)
    }

}