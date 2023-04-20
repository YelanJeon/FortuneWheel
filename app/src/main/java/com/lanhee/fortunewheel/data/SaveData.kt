package com.lanhee.fortunewheel.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "list")
class SaveData (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name="title") val title: String,
    @ColumnInfo(name="items") val items: Array<String>
)