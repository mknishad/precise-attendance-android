package com.monir.preciseattendance.domain.repository

import com.monir.preciseattendance.domain.model.OfficeLocation
import java.io.IOException
import kotlinx.coroutines.flow.Flow

interface OfficeLocationRepository {
    /** Emits the saved office location, or null when none has been set or it cannot be read. */
    fun observeOfficeLocation(): Flow<OfficeLocation?>

    /** @throws IOException if the location cannot be written. */
    suspend fun saveOfficeLocation(office: OfficeLocation)
}
