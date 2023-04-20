package com.lanhee.fortunewheel.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lanhee.fortunewheel.data.SaveConverters
import com.lanhee.fortunewheel.data.SaveDao
import com.lanhee.fortunewheel.data.SaveData

@Database(entities = [SaveData::class], version = 1)
@TypeConverters(SaveConverters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun saveDao(): SaveDao

    companion object {
        private var database: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if(database == null) {
                synchronized(AppDatabase::class.java) {
                    database = Room.databaseBuilder(
                                    context,
                                    AppDatabase::class.java,
                                    "FortuneWheel"
                                ).build()
                }
            }
            return database
        }
    }

}