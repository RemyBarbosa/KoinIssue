package fr.radiofrance.alarm.data.repository

import fr.radiofrance.alarm.data.datasource.room.AlarmDAO
import fr.radiofrance.alarm.domain.AlarmMapper
import fr.radiofrance.alarm.domain.AlarmModel
import io.reactivex.Maybe

interface AlarmRepository {
    fun getNextAlarm(currentTimeMillis: Long): Maybe<AlarmModel>
}

class AlarmRepositoryImpl(
        private val alarmDAO: AlarmDAO
) : AlarmRepository {
    override fun getNextAlarm(currentTimeMillis: Long): Maybe<AlarmModel> {
        /* BY JCD */
        return alarmDAO.findAll().flatMapMaybe {
            it.map { AlarmMapper.modelFrom(it) }
                    .filter { it.enable }
                    .sortedBy { it.nextAlarmMillis(currentTimeMillis) }
                    .firstOrNull()?.let { Maybe.just(it) } ?: Maybe.empty()
        }
    }
}

