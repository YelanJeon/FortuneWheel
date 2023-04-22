package com.lanhee.fortunewheel

import android.app.Application
import com.lanhee.fortunewheel.utils.AppDatabase
import com.lanhee.fortunewheel.utils.PreferenceHelper

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this)
        AppDatabase.init(this)
    }
}