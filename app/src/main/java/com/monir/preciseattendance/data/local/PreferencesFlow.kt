package com.monir.preciseattendance.data.local

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Treats an unreadable store as an empty one so the app falls back to its "nothing saved" state
 * and the user can set things again, instead of crashing the collector. Only [IOException]
 * (which includes DataStore's corruption exception) is handled; anything else still propagates.
 */
fun Flow<Preferences>.emptyOnReadFailure(): Flow<Preferences> = catch { cause ->
    if (cause is IOException) emit(emptyPreferences()) else throw cause
}
