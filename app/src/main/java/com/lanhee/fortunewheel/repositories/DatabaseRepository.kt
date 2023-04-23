package com.lanhee.fortunewheel.repositories

import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.utils.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DatabaseRepository(private val database: AppDatabase) {
    fun saveItems(title: String, items: Array<String>) {
        val saveData = SaveData(title=title, items=items)
        database.saveDao().insert(saveData)
    }

    fun loadData(): MutableList<SaveData> {
        return database.saveDao().getAll().toMutableList()
    }

    fun deleteData(target: SaveData) {
        database.saveDao().delete(target)
    }
}