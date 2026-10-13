package com.monir.preciseattendance.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.monir.preciseattendance.domain.model.OfficeLocation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class OfficeLocationStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun observe(): Flow<OfficeLocation?> {
        TODO("Map preferences to OfficeLocation, or null when no key is present")
    }

    suspend fun save(office: OfficeLocation) {
        TODO("Write the office point to DataStore via dataStore.edit")
    }

    private companion object Keys {
        val LATITUDE = doublePreferencesKey("office_latitude")
        val LONGITUDE = doublePreferencesKey("office_longitude")
        val SAVED_AT = longPreferencesKey("office_saved_at_epoch_millis")
    }
}
