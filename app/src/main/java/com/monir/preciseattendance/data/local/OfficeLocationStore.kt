package com.monir.preciseattendance.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.OfficeLocation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists the office point. Coordinates are stored as Doubles, with no rounding or formatting. */
@Singleton
class OfficeLocationStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** Emits the saved office, or null when it has never been set or any part of it is missing. */
    fun observe(): Flow<OfficeLocation?> =
        dataStore.data.emptyOnReadFailure().map { prefs -> prefs.toOfficeLocation() }

    /** @throws java.io.IOException if the write fails. */
    suspend fun save(office: OfficeLocation) {
        dataStore.edit { prefs ->
            prefs[LATITUDE] = office.point.latitude
            prefs[LONGITUDE] = office.point.longitude
            prefs[SAVED_AT] = office.savedAtMillis
        }
    }

    private fun Preferences.toOfficeLocation(): OfficeLocation? {
        val latitude = this[LATITUDE] ?: return null
        val longitude = this[LONGITUDE] ?: return null
        val savedAt = this[SAVED_AT] ?: return null
        return OfficeLocation(GeoPoint(latitude, longitude), savedAt)
    }

    private companion object Keys {
        val LATITUDE = doublePreferencesKey("office_latitude")
        val LONGITUDE = doublePreferencesKey("office_longitude")
        val SAVED_AT = longPreferencesKey("office_saved_at_epoch_millis")
    }
}
