package com.monir.preciseattendance.data.location

import com.google.android.gms.location.FusedLocationProviderClient
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
) : LocationRepository {

    override fun observeLocation(): Flow<GeoPoint> = callbackFlow {
        // TODO: build a LocationRequest with Priority.PRIORITY_HIGH_ACCURACY, register a
        //  LocationCallback that trySend()s each fix, and removeLocationUpdates() in awaitClose.
        awaitClose { }
    }

    override fun observeLocationServicesEnabled(): Flow<Boolean> {
        TODO("Observe location services state (e.g. LocationAvailability / SettingsClient)")
    }

    override suspend fun getCurrentLocation(): GeoPoint {
        TODO("Request a single high-accuracy fix from fusedLocationClient")
    }
}
