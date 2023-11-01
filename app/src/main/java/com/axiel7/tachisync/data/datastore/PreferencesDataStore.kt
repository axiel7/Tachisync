package com.axiel7.tachisync.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object PreferencesDataStore {

    val EXTERNAL_URI_KEY = stringPreferencesKey("external_uri")
    val TACHIYOMI_URI_KEY = stringPreferencesKey("tachiyomi_uri")

    val Context.defaultPreferencesDataStore by preferencesDataStore(name = "default")
}