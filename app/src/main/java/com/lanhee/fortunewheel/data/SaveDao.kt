package com.lanhee.fortunewheel.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SaveDao {
    @Query("SELECT * FROM list")
    fun getAll(): Array<SaveData>

    @Insert
    fun insert(data: SaveData)

    @Delete
    fun delete(data: SaveData)
}