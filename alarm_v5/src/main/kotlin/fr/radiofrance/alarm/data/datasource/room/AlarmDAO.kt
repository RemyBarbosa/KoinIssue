package fr.radiofrance.alarm.data.datasource.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface AlarmDAO {

    @Insert
    fun saveAll(entities: List<AlarmEntity>)

    @Query("SELECT * FROM alarm")
    fun findAll(): Single<List<AlarmEntity>>

    @Query("SELECT * FROM alarm WHERE id = :id")
    fun findAlarmById(id: String): Single<AlarmEntity>
}