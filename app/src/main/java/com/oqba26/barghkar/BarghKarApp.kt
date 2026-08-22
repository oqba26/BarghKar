package com.oqba26.barghkar

import android.app.Application
import com.oqba26.barghkar.data.SettingsManager

class BarghKarApp : Application() {
    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
    }
}
