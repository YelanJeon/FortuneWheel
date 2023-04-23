package com.lanhee.fortunewheel.screen.inputdialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InputDialogViewModel: ViewModel() {
    private val _inputText = MutableLiveData<String>()
    val inputText = _inputText as LiveData<String>

    fun setInputText(text: String) {
        _inputText.value = text
    }

    @Suppress("UNCHECKED_CAST")
    class Factory: ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InputDialogViewModel() as T
        }
    }
}