package com.monir.preciseattendance.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AttendanceStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun nothingRecorded_observesNull_thenTheLastTimestamp() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("attendance.preferences_pb") },
        )
        val store = AttendanceStore(dataStore)
        assertNull(store.observeLastMarkedAtMillis().first())

        store.saveLastMarkedAtMillis(1_760_484_600_123L)
        assertEquals(1_760_484_600_123L, store.observeLastMarkedAtMillis().first())

        store.saveLastMarkedAtMillis(1_760_571_000_000L)
        assertEquals(1_760_571_000_000L, store.observeLastMarkedAtMillis().first())
    }
}
