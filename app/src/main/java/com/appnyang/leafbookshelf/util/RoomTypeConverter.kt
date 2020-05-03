package com.appnyang.leafbookshelf.util

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Object to String or vice versa converter for Room model.
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

    @TypeConverter
    fun dateTimeToString(time: DateTime): String = time.toString(ISODateTimeFormat.dateTime())

    @TypeConverter
    fun stringToDateTime(time: String): DateTime = ISODateTimeFormat.dateTime().parseDateTime(time)

    @TypeConverter
    fun uriToString(uri: Uri): String = uri.toString()

    @TypeConverter
    fun stringToUri(uri: String): Uri = Uri.parse(uri)
}
