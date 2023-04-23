package com.lanhee.fortunewheel.screen.listdialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ListDialogViewModel: ViewModel() {
    private val _items = MutableLiveData<Array<String>>()
    val items = _items as LiveData<Array<String>>

    private val _inputText = MutableLiveData<String>()
    val inputText = _inputText as LiveData<String>

    fun setItems(items: Array<String>) {
        _items.value = items
    }

    fun addItem(new: String) {
        val temp = MutableList(_items.value!!.size) { _items.value!![it] }
        temp.add(new)
        setItems(temp.toTypedArray())
    }

    fun deleteItem(target: String) {
        val temp = MutableList(_items.value!!.size) { _items.value!![it] }
        temp.remove(target)
        setItems(temp.toTypedArray())
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListDialogViewModel() as T
        }
    }
}