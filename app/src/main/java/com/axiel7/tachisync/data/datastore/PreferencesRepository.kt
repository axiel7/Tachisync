package com.axiel7.tachisync.data.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.axiel7.tachisync.App
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object PreferencesRepository {

    private val store get() = App.dataStore

    suspend fun <T> get(key: Preferences.Key<T>) = store.data.first()[key]

    /**
     * Gets the value by blocking the main thread
     */
    fun <T> getSync(key: Preferences.Key<T>) = runBlocking { get(key) }

    suspend fun <T> set(
        key: Preferences.Key<T>,
        value: T
    ) = store.edit { it[key] = value }

    /**
     * Sets the value by blocking the main thread
     */
    fun <T> setSync(
        key: Preferences.Key<T>,
        value: T
    ) = runBlocking { set(key, value) }

    suspend fun <T> remove(key: Preferences.Key<T>) = store.edit { it.remove(key) }

    /**
     * Removes the value by blocking the main thread
     */
    fun <T> removeSync(key: Preferences.Key<T>) = runBlocking { remove(key) }
}