package com.lanhee.fortunewheel.screen.loaddialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.repositories.DatabaseRepository
import com.lanhee.fortunewheel.utils.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadDialogViewModel: ViewModel() {
    private val _list = MutableLiveData<List<SaveData>>()
    val list = _list as LiveData<List<SaveData>>

    private val databaseRepository by lazy { DatabaseRepository(AppDatabase.getInstance()) }

    fun loadData() {
        viewModelScope.launch {
            val temp = databaseRepository.loadData()
            _list.value = temp
        }
    }

    fun deleteData(target: SaveData) {
        viewModelScope.launch {
            databaseRepository.deleteData(target)

            val temp = MutableList(_list.value!!.size) {
                _list.value!![it]
            }
            temp.remove(target)

            _list.value = temp
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
    ) :ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoadDialogViewModel() as T
        }
    }
}