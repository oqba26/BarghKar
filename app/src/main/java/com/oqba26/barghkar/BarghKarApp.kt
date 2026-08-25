package com.oqba26.barghkar

import android.app.Application
import com.oqba26.barghkar.data.SettingsManager
import com.oqba26.barghkar.data.remote.SupabaseClient
import net.sqlcipher.database.SQLiteDatabase

class BarghKarApp : Application() {
    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        SupabaseClient.initialize(this)
        settingsManager = SettingsManager(this)
    }
}
