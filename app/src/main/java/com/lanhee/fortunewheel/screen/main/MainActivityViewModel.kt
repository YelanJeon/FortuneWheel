package com.lanhee.fortunewheel.screen.main

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanhee.fortunewheel.repositories.DatabaseRepository
import com.lanhee.fortunewheel.utils.AppDatabase
import com.lanhee.fortunewheel.utils.PreferenceHelper
import com.lanhee.fortunewheel.utils.ScreenCaptureHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {
    private val _isSettingMode = MutableLiveData<Boolean>()
    val isSettingMode = _isSettingMode as LiveData<Boolean>

    private val _items = MutableLiveData<Array<String>>()
    val items = _items as LiveData<Array<String>>

    private val _selectedText = MutableLiveData<String>()
    val selectedText = _selectedText as LiveData<String>

    private val _intentToShare = MutableLiveData<Intent>()
    val intentToShare = _intentToShare as LiveData<Intent>

    private val _intentToUpdate = MutableLiveData<Intent>()
    val intentToUpdate = _intentToUpdate as LiveData<Intent>

    private val databaseRepository by lazy { DatabaseRepository(AppDatabase.getInstance()) }

    fun setItem(text: String, position: Int) {
        val temp = Array(_items.value!!.size) { index -> _items.value!![index] }
        temp[position] = text
        _items.value = temp
    }

    fun setItems(items: Array<String>) {
        _items.value = items
    }

    fun setSettingMode(isSettingMode: Boolean) {
        _isSettingMode.value = isSettingMode
    }

    fun isSettingMode(): Boolean {
        return isSettingMode.value?:true
    }

    fun setSelectedText(text: String) {
        _selectedText.value = text
    }

    fun setDefaultSettings() {
        var defaultList = PreferenceHelper.loadListToPreferences()
        if(defaultList.isEmpty()) {
            defaultList = arrayOf("Y", "N")
        }
        _items.value = defaultList
    }

    fun saveListToPrefences() {
        PreferenceHelper.saveListToPreferences(_items.value?.size?:0, _items.value?: arrayOf("Y", "N"))
    }

    fun saveItems(title: String) {
        viewModelScope.launch {
            databaseRepository.saveItems(title, _items.value!!)
        }
    }

    fun captureAndShare(captureable: ScreenCaptureHelper.Captureable) {
        ScreenCaptureHelper(captureable.getCaptureView()).captureAndSave(object : ScreenCaptureHelper.OnCaptureView {
            override fun onCapture(uri: Uri) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = "image/*"
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                _intentToShare.value = intent

            }
        })
    }
}