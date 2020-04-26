package com.appnyang.leafbookshelf.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Object to JSON or vice versa converter for Room model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-25.
 */
class RoomTypeConverter {

    @TypeConverter
    fun stringListToJson(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToStringList(json: String): List<String> {
        return Gson().fromJson(json, object : TypeToken<Collection<String>>() {}.type)
    }
}