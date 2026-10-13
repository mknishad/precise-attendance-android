package com.monir.preciseattendance.domain.repository

import com.monir.preciseattendance.domain.model.OfficeLocation
import kotlinx.coroutines.flow.Flow

interface OfficeLocationRepository {
    /** Emits the saved office location, or null when none has been set. */
    fun observeOfficeLocation(): Flow<OfficeLocation?>

    suspend fun saveOfficeLocation(office: OfficeLocation)
}
