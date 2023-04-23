package com.lanhee.fortunewheel.repositories

import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.utils.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseRepository(private val database: AppDatabase) {
    fun saveItems(title: String, items: Array<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val saveData = SaveData(title=title, items=items)
            val db = AppDatabase.getInstance()
            db.saveDao().insert(saveData)
        }
    }
}