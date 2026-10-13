package com.monir.preciseattendance.fakes

import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationFix
import com.monir.preciseattendance.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class FakeLocationRepository : LocationRepository {
    val fixes = MutableSharedFlow<LocationFix>()

    /** Replace to make [locationUpdates] fail or end differently from the default shared flow. */
    var updates: Flow<LocationFix> = fixes

    var currentLocation: FixResult = FixResult.Success(testFix())
    var onGetCurrentLocation: () -> Unit = {}
    var getCurrentLocationCalls = 0
        private set

    val locationEnabled = MutableStateFlow(true)

    /** Waits until something is collecting [locationUpdates], so an emission cannot be missed. */
    suspend fun emitFix(fix: LocationFix) {
        fixes.subscriptionCount.first { it > 0 }
        fixes.emit(fix)
    }

    override fun locationUpdates(): Flow<LocationFix> = updates

    override suspend fun getCurrentLocation(): FixResult {
        getCurrentLocationCalls++
        onGetCurrentLocation()
        return currentLocation
    }

    override fun isLocationEnabled(): Flow<Boolean> = locationEnabled
}
