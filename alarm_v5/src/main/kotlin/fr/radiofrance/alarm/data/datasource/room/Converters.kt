package fr.radiofrance.alarm.data.datasource.room

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        val listType = object : TypeToken<ArrayList<Int>>() {}.type
        return Gson().fromJson<ArrayList<Int>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: List<Int>): String {
        return Gson().toJson(list)
    }
}