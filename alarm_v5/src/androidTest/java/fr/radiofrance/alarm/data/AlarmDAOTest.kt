package fr.radiofrance.alarm.data

import android.app.Application
import android.support.test.runner.AndroidJUnit4
import fr.radiofrance.alarm.data.datasource.room.AlarmDAO
import fr.radiofrance.alarm.data.datasource.room.AlarmDatabase
import fr.radiofrance.alarm.data.datasource.room.AlarmEntity
import fr.radiofrance.di.roomTestModule
import junit.framework.Assert
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.with
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.mockito.Mockito
import java.util.*

/**
 * AlarmDAOTest is a KoinTest with AndroidJUnit4 runner
 *
 * KoinTest help inject Koin components from actual runtime
 */
@RunWith(AndroidJUnit4::class)
class AlarmDAOTest : KoinTest {

    val alarmDatabase: AlarmDatabase by inject()
    val alarmDao: AlarmDAO by inject()

    @Before
    fun before() {
        startKoin(listOf(roomTestModule)) with Mockito.mock(Application::class.java)
    }

    @After
    fun after() {
        alarmDatabase.close()
        closeKoin()
    }

    @Test
    fun testFindById() {
        // given
        val alarm1 = AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 19, minute = 34, enable = true)
        val alarm2 = AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 15, minute = 39, enable = true)
        val alarms = listOf(alarm1, alarm2)

        // when
        alarmDao.saveAll(alarms)
        val alarm2ById = alarmDao.findAlarmById(alarm2.id).blockingGet()

        // then
        Assert.assertEquals(alarm2, alarm2ById)
    }

    @Test
    fun testSaveAll() {
        // given
        val alarm1 = AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 19, minute = 34, enable = true)
        val alarm2 = AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 15, minute = 39, enable = true)
        val alarms = listOf(alarm1, alarm2)

        // when
        alarmDao.saveAll(alarms)
        val ids = alarms.map { it.id }
        val requestedEntities = ids.map { alarmDao.findAlarmById(it).blockingGet() }

        // then
        Assert.assertEquals(alarms, requestedEntities)
    }

    @Test
    fun testFindAll() {
        // given
        val alarm1 = AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 19, minute = 34, enable = true)
        val alarm2 = AlarmEntity(days = listOf(Calendar.MONDAY, Calendar.THURSDAY), hour = 15, minute = 39, enable = true)
        val alarms = listOf(alarm1, alarm2)


        // when
        alarmDao.saveAll(alarms)
        val alarmsFromDao = alarmDao.findAll().blockingGet()

        // then
        Assert.assertEquals(alarms, alarmsFromDao)
    }
}