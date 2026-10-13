package com.monir.preciseattendance.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists only the most recent attendance timestamp; "marked today" is derived from it. */
@Singleton
class AttendanceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeLastMarkedAtMillis(): Flow<Long?> =
        dataStore.data.emptyOnReadFailure().map { prefs -> prefs[LAST_MARKED_AT] }

    /** @throws java.io.IOException if the write fails. */
    suspend fun saveLastMarkedAtMillis(markedAtMillis: Long) {
        dataStore.edit { prefs -> prefs[LAST_MARKED_AT] = markedAtMillis }
    }

    private companion object Keys {
        val LAST_MARKED_AT = longPreferencesKey("last_marked_at_epoch_millis")
    }
}
