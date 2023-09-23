package com.axiel7.tachisync

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.axiel7.tachisync.data.datastore.PreferencesDataStore.defaultPreferencesDataStore

class App : Application() {

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        dataStore = defaultPreferencesDataStore
    }

    companion object {
        lateinit var INSTANCE: App
            private set
        val applicationContext: Context get() = INSTANCE.applicationContext

        lateinit var dataStore: DataStore<Preferences>
    }
}