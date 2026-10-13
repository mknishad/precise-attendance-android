package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import javax.inject.Inject

class SetOfficeLocation @Inject constructor(
    private val locationRepository: LocationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
) {
    /** Saves the device's current location as the office point. */
    suspend operator fun invoke() {
        TODO("Read getCurrentLocation() and persist it via saveOfficeLocation()")
    }
}
