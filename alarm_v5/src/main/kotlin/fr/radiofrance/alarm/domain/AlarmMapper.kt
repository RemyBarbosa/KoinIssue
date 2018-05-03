package fr.radiofrance.alarm.domain

import fr.radiofrance.alarm.data.datasource.room.AlarmEntity

object AlarmMapper {
    fun modelFrom(entity: AlarmEntity) = AlarmModel(
            entity.id,
            entity.days,
            entity.hour,
            entity.minute,
            entity.enable,
            entity.snoozeAtMillis
    )
}