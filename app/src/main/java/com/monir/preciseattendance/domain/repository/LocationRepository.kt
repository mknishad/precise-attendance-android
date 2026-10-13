package com.monir.preciseattendance.domain.repository

import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationFix
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /**
     * Live high-accuracy fixes; the provider is active only while the flow is collected.
     * If updates cannot start or stop, the flow terminates with a
     * [com.monir.preciseattendance.domain.model.LocationException] carrying the reason.
     */
    fun locationUpdates(): Flow<LocationFix>

    /** A fresh one-shot fix, used for "Set Office Location" and at the moment of marking. */
    suspend fun getCurrentLocation(): FixResult

    /** Emits whether device location services are on, and again whenever that changes. */
    fun isLocationEnabled(): Flow<Boolean>
}
