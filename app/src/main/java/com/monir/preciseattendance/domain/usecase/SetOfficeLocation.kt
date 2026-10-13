package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.LocationFix
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.hasAcceptableAccuracy
import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import java.io.IOException
import java.time.Clock
import javax.inject.Inject

sealed interface SetOfficeLocationResult {
    data class Success(val office: OfficeLocation) : SetOfficeLocationResult
    data class WeakSignal(val accuracyMeters: Double) : SetOfficeLocationResult
    data class LocationUnavailable(val error: LocationError) : SetOfficeLocationResult
    data object StorageFailure : SetOfficeLocationResult
}

class SetOfficeLocation @Inject constructor(
    private val locationRepository: LocationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val clock: Clock,
) {
    /** Saves the device's current location as the office, unless the fix is too inaccurate. */
    suspend operator fun invoke(): SetOfficeLocationResult =
        when (val result = locationRepository.getCurrentLocation()) {
            is FixResult.Failure -> SetOfficeLocationResult.LocationUnavailable(result.error)
            is FixResult.Success -> save(result.fix)
        }

    private suspend fun save(fix: LocationFix): SetOfficeLocationResult {
        if (!fix.hasAcceptableAccuracy()) return SetOfficeLocationResult.WeakSignal(fix.accuracyMeters)
        val office = OfficeLocation(point = fix.point, savedAtMillis = clock.millis())
        return try {
            officeLocationRepository.saveOfficeLocation(office)
            SetOfficeLocationResult.Success(office)
        } catch (e: IOException) {
            SetOfficeLocationResult.StorageFailure
        }
    }
}
