package fr.radiofrance.alarm.data

import fr.radiofrance.alarm.data.datasource.room.AlarmDAO
import fr.radiofrance.alarm.data.datasource.room.AlarmEntity
import fr.radiofrance.alarm.data.repository.AlarmRepository
import fr.radiofrance.alarm.data.repository.AlarmRepositoryImpl
import fr.radiofrance.alarm.domain.AlarmMapper
import io.reactivex.Single
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * AlarmDAOTest is a KoinTest with AndroidJUnit4 runner
 *
 * KoinTest help inject Koin components from actual runtime
 */

@RunWith(Parameterized::class)
class AlarmRepositoryTestParameterized(
        val hourOfNow: Int,
        val minuteOfNow: Int,
        val alarm1: AlarmEntity,
        val alarm2: AlarmEntity
) : KoinTest {
    companion object {
        const val YEAR = 2018
        const val MONTH = Calendar.MARCH
        const val DAY_OF_MONTH_WEDNESDAY = 14
        const val TIME_NOW_15_H_IN_MILLIS = 1521039600000
        const val TIME_NOW_15_H_IN_MILLIS_PLUS_ONE_MINUTE = TIME_NOW_15_H_IN_MILLIS + 60000

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: now : {0}h{1} alarm1 : {2} alarm2 : {3}")
        fun data(): Collection<Array<Any>> {
            return Arrays.asList(
                    arrayOf(15, 0,
                            AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 15, minute = 30),
                            AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 19, minute = 34)),
                    arrayOf(15, 0,
                            AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 19, minute = 34),
                            AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 15, minute = 30, enable = false)),


                    arrayOf(15, 0,
                            AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 19, minute = 34),
                            AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.WEDNESDAY), hour = 14, minute = 30)),
                    arrayOf(15, 0,
                            AlarmEntity(days = listOf(Calendar.MONDAY), hour = 19, minute = 34),
                            AlarmEntity(days = listOf(Calendar.TUESDAY), hour = 14, minute = 30)),

                    arrayOf(15, 0,
                            AlarmEntity(days = listOf(Calendar.TUESDAY), hour = 14, minute = 30, snoozeAtMillis = TIME_NOW_15_H_IN_MILLIS_PLUS_ONE_MINUTE),
                            AlarmEntity(days = listOf(Calendar.MONDAY), hour = 19, minute = 34))

            )

        }
    }

    lateinit var alarmRepository: AlarmRepository
    @Mock
    lateinit var alarmDao: AlarmDAO

    @Before()
    fun before() {
        MockitoAnnotations.initMocks(this)
        alarmRepository = AlarmRepositoryImpl(alarmDao)
    }

    @Test
    fun textGetNextAlarm() {
        // given
        val now = GregorianCalendar(YEAR, MONTH, DAY_OF_MONTH_WEDNESDAY, hourOfNow, minuteOfNow, 0)
        val alarms = listOf(alarm1, alarm2)

        // when
        given(alarmDao.findAll()).willReturn(Single.just(alarms))

        // then
        Assert.assertEquals(
                alarmRepository.getNextAlarm(now.timeInMillis).blockingGet().nextAlarmMillis(now.timeInMillis),
                AlarmMapper.modelFrom(alarm1).nextAlarmMillis(now.timeInMillis)
        )
    }
}