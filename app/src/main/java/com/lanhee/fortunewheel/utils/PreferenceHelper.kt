package com.lanhee.fortunewheel.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

object PreferenceHelper {
    //preferences용 상수
    const val _PREF_NAME_DEFAULT = "defaultSetting"
    const val _P_SIZE = "size"
    const val _P_ITEM = "item"

    private lateinit var pref: SharedPreferences

    fun init(context: Context) {
        pref = context.getSharedPreferences(_PREF_NAME_DEFAULT, Activity.MODE_PRIVATE)
    }

    fun loadListToPreferences(): Array<String> {
        val result = Array(pref.getInt(_P_SIZE, 0)) { index ->
            pref.getString(_P_ITEM +index, null)?:""
        }
        return result
    }

    fun saveListToPreferences(size: Int, items:Array<String>) {
        val editor = pref.edit()
        editor.putInt(_P_SIZE, size)
        items.forEachIndexed { index, item ->
            editor.putString(_P_ITEM +index, item)
        }
        editor.apply()
    }
}