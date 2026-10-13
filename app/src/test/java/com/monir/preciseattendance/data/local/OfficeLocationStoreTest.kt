package com.monir.preciseattendance.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.OfficeLocation
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OfficeLocationStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun TestScope.dataStore(file: File = temporaryFolder.newFile("test.preferences_pb")): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { file })

    @Test
    fun nothingSaved_observesNull() = runTest {
        assertNull(OfficeLocationStore(dataStore()).observe().first())
    }

    @Test
    fun saved_roundTripsCoordinatesWithoutLosingPrecision() = runTest {
        val store = OfficeLocationStore(dataStore())
        val office = OfficeLocation(
            point = GeoPoint(latitude = -37.81362765432101, longitude = 144.96305761234567),
            savedAtMillis = 1_760_484_600_123L,
        )

        store.save(office)

        val restored = store.observe().first()!!
        // Delta of zero: any trip through Float or a formatted string would fail this.
        assertEquals(office.point.latitude, restored.point.latitude, 0.0)
        assertEquals(office.point.longitude, restored.point.longitude, 0.0)
        assertEquals(office.savedAtMillis, restored.savedAtMillis)
    }

    @Test
    fun adjacentDoubles_stayDistinct() = runTest {
        val store = OfficeLocationStore(dataStore())
        val latitude = Math.nextUp(-37.8136)

        store.save(OfficeLocation(GeoPoint(latitude, Math.nextDown(144.9631)), savedAtMillis = 1L))

        val restored = store.observe().first()!!
        assertEquals(latitude.toRawBits(), restored.point.latitude.toRawBits())
        assertEquals(Math.nextDown(144.9631).toRawBits(), restored.point.longitude.toRawBits())
    }

    @Test
    fun savingAgain_replacesThePreviousOffice() = runTest {
        val store = OfficeLocationStore(dataStore())
        store.save(OfficeLocation(GeoPoint(1.0, 2.0), savedAtMillis = 10L))

        val second = OfficeLocation(GeoPoint(3.0, 4.0), savedAtMillis = 20L)
        store.save(second)

        assertEquals(second, store.observe().first())
    }

    @Test
    fun partiallyWrittenData_observesNull() = runTest {
        val dataStore = dataStore()
        dataStore.edit { it[doublePreferencesKey("office_latitude")] = 1.0 }

        assertNull(OfficeLocationStore(dataStore).observe().first())
    }

    @Test
    fun unreadableFile_observesNullInsteadOfThrowing() = runTest {
        val corrupt = temporaryFolder.newFile("corrupt.preferences_pb")
        corrupt.writeBytes(byteArrayOf(-1, -1, -1, -1, -1, -1, -1, -1))

        assertNull(OfficeLocationStore(dataStore(corrupt)).observe().first())
    }
}
