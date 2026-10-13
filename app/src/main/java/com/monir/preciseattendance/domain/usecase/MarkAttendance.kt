package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.geo.ProximityEvaluator
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.LocationFix
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import java.io.IOException
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.first

sealed interface MarkAttendanceResult {
    data class Success(val markedAtMillis: Long) : MarkAttendanceResult
    data object NoOfficeSet : MarkAttendanceResult
    data class OutsideAttendanceWindow(val window: AttendanceWindow) : MarkAttendanceResult
    data class WeakSignal(val accuracyMeters: Double) : MarkAttendanceResult
    data class OutOfRange(val distanceMeters: Double) : MarkAttendanceResult
    data class LocationUnavailable(val error: LocationError) : MarkAttendanceResult
    data object StorageFailure : MarkAttendanceResult
}

/**
 * Re-validates everything at the moment of the tap instead of trusting the last UI state: the
 * office, the time window and a fresh high-accuracy fix.
 */
class MarkAttendance @Inject constructor(
    private val locationRepository: LocationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val attendanceRepository: AttendanceRepository,
    private val proximityEvaluator: ProximityEvaluator,
    private val window: AttendanceWindow,
    private val clock: Clock,
) {
    suspend operator fun invoke(): MarkAttendanceResult {
        val office = officeLocationRepository.observeOfficeLocation().first()
            ?: return MarkAttendanceResult.NoOfficeSet
        // Checked before the fix to fail fast, and again after it because a fix can take seconds.
        if (!window.isOpen(clock)) return MarkAttendanceResult.OutsideAttendanceWindow(window)

        return when (val result = locationRepository.getCurrentLocation()) {
            is FixResult.Failure -> MarkAttendanceResult.LocationUnavailable(result.error)
            is FixResult.Success -> validateAndRecord(office, result.fix)
        }
    }

    private suspend fun validateAndRecord(office: OfficeLocation, fix: LocationFix): MarkAttendanceResult {
        if (!window.isOpen(clock)) return MarkAttendanceResult.OutsideAttendanceWindow(window)
        return when (val proximity = proximityEvaluator.evaluate(office, fix)) {
            is ProximityResult.InRange -> record()
            is ProximityResult.OutOfRange -> MarkAttendanceResult.OutOfRange(proximity.distanceMeters)
            is ProximityResult.WeakSignal -> MarkAttendanceResult.WeakSignal(proximity.accuracyMeters)
            // The office and fix are both non-null here, so these cannot occur; they are listed so a
            // new ProximityResult subtype forces this mapping to be revisited.
            ProximityResult.NoOfficeSet -> MarkAttendanceResult.NoOfficeSet
            ProximityResult.AcquiringFix,
            is ProximityResult.LocationUnavailable,
            -> MarkAttendanceResult.LocationUnavailable(LocationError.Unavailable)
        }
    }

    private suspend fun record(): MarkAttendanceResult {
        val markedAtMillis = clock.millis()
        return try {
            attendanceRepository.recordAttendance(markedAtMillis)
            MarkAttendanceResult.Success(markedAtMillis)
        } catch (e: IOException) {
            MarkAttendanceResult.StorageFailure
        }
    }
}
