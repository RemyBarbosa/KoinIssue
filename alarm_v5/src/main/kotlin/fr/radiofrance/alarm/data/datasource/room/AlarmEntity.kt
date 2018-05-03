package fr.radiofrance.alarm.data.datasource.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "alarm")
data class AlarmEntity(
        @PrimaryKey
        val id: String = UUID.randomUUID().toString(),
        var days: List<Int> = ArrayList(),
        var hour: Int,
        var minute: Int,
        var enable: Boolean = true,
        var snoozeAtMillis: Long = -1
)