package com.monir.preciseattendance.domain.repository

import com.monir.preciseattendance.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /** Live high-accuracy location updates; active only while collected. */
    fun observeLocation(): Flow<GeoPoint>

    /** Emits whether device location services (GPS) are switched on. */
    fun observeLocationServicesEnabled(): Flow<Boolean>

    /** One-shot fix, used when saving the office location. */
    suspend fun getCurrentLocation(): GeoPoint
}
