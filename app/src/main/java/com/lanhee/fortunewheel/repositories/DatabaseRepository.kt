package com.lanhee.fortunewheel.repositories

import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.utils.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseRepository(private val database: AppDatabase) {
    suspend fun saveItems(title: String, items: Array<String>) = withContext(database.ioDispatcher) {
                                                                     val saveData = SaveData(title=title, items=items)
                                                                     database.saveDao().insert(saveData)
                                                                 }

    suspend fun loadData() : List<SaveData> = withContext(database.ioDispatcher) {
                                                  database.saveDao().getAll().toList()
                                              }

    suspend fun deleteData(target: SaveData) = withContext(database.ioDispatcher) {
                                                   database.saveDao().delete(target)
                                               }
}