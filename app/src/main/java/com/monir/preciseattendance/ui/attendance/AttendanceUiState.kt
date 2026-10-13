package com.monir.preciseattendance.ui.attendance

import androidx.compose.runtime.Immutable
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.ProximityResult

/** Distance at which the progress ring is empty. Closer fills it, 0 m being full. */
const val DISTANCE_RING_MAX_METERS: Double = 200.0

/**
 * One data class rather than a sealed hierarchy: permission, GPS, proximity and the time window
 * vary independently, so a hierarchy would need a case for every combination. The single value
 * the UI actually switches on, [statusMessage], is derived from them.
 *
 * The derived properties are computed from the stored ones, so they can never disagree.
 */
@Immutable
data class AttendanceUiState(
    val permissionState: LocationPermissionState = LocationPermissionState.NotRequested,
    val isLocationEnabled: Boolean = false,
    val officeLocation: OfficeLocation? = null,
    val proximity: ProximityResult = ProximityResult.AcquiringFix,
    val isSettingOffice: Boolean = false,
    val isMarkingAttendance: Boolean = false,
    val attendanceWindow: AttendanceWindow = AttendanceWindow(),
    val isWithinWindow: Boolean = false,
    val alreadyMarkedToday: Boolean = false,
) {
    /** Metres to the office when there is a trustworthy, finite measurement; otherwise null. */
    val distanceMeters: Double?
        get() = when (val result = proximity) {
            is ProximityResult.InRange -> result.distanceMeters
            is ProximityResult.OutOfRange -> result.distanceMeters
            else -> null
        }?.takeIf { it.isFinite() }

    val distanceProgress: Float
        get() = distanceProgressFor(distanceMeters)

    /** Derived only from the five conditions that must all hold. */
    val isMarkAttendanceEnabled: Boolean
        get() = permissionState == LocationPermissionState.Granted &&
            isLocationEnabled &&
            proximity is ProximityResult.InRange &&
            isWithinWindow &&
            !alreadyMarkedToday

    val statusMessage: StatusMessage
        get() = resolveStatusMessage()
}

/** 1f at 0 m down to 0f at [DISTANCE_RING_MAX_METERS] or beyond; 0f when there is no distance. */
fun distanceProgressFor(distanceMeters: Double?): Float {
    if (distanceMeters == null || distanceMeters.isNaN()) return 0f
    return (1.0 - distanceMeters / DISTANCE_RING_MAX_METERS).coerceIn(0.0, 1.0).toFloat()
}
