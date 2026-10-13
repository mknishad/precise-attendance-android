package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveDistanceToOffice @Inject constructor(
    private val locationRepository: LocationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
) {
    /** Emits the live distance in metres from the current location to the saved office point. */
    operator fun invoke(): Flow<Double> {
        TODO("Combine observeLocation() with observeOfficeLocation() and compute the distance")
    }
}
