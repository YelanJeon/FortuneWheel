package com.lanhee.fortunewheel.data

import androidx.room.TypeConverter
import com.google.gson.Gson

class SaveConverters {
    @TypeConverter
    fun listToJson(items: Array<String>): String  = Gson().toJson(items)

    @TypeConverter
    fun jsonToList(json: String) : Array<String>  = Gson().fromJson(json, Array<String>::class.java)
}