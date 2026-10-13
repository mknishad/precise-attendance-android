package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.domain.model.AttendancePolicy
import com.monir.preciseattendance.domain.model.ProximityResult
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * The single standing message for the current state. Earlier checks win: nothing about distance
 * means anything until permission and location services are sorted out.
 */
internal fun AttendanceUiState.resolveStatusMessage(): StatusMessage {
    permissionState.missingMessage()?.let { return it }
    if (!isLocationEnabled) return StatusMessage.LocationOff
    return when (val result = proximity) {
        ProximityResult.NoOfficeSet -> StatusMessage.NoOfficeSet
        ProximityResult.AcquiringFix -> StatusMessage.AcquiringFix
        is ProximityResult.WeakSignal -> StatusMessage.WeakSignal(result.accuracyMeters.accuracyToWholeMetersOrNull())
        is ProximityResult.LocationUnavailable -> StatusMessage.LocationUnavailable
        is ProximityResult.OutOfRange -> outOfRangeMessage(result.distanceMeters)
        is ProximityResult.InRange -> inRangeMessage()
    }
}

private fun AttendanceUiState.inRangeMessage(): StatusMessage = when {
    alreadyMarkedToday -> StatusMessage.AlreadyMarkedToday
    !isWithinWindow -> StatusMessage.OutsideWindow(attendanceWindow.start, attendanceWindow.end)
    else -> distanceMeters?.let { StatusMessage.InRange(it.roundToWholeMeters()) } ?: StatusMessage.LocationUnavailable
}

/**
 * Distances are rounded to the nearest metre, except that an out-of-range distance never shows as
 * inside the limit: 50.1 m reads as 51 m, so the text cannot contradict the disabled button.
 */
internal fun outOfRangeMessage(distanceMeters: Double): StatusMessage {
    if (!distanceMeters.isFinite()) return StatusMessage.LocationUnavailable
    val firstOutsideMeters = floor(AttendancePolicy.ALLOWED_RADIUS_METERS).toInt() + 1
    return StatusMessage.OutOfRange(max(distanceMeters.roundToWholeMeters(), firstOutsideMeters))
}

/** The message explaining why precise location is not available, or null when it is. */
fun LocationPermissionState.missingMessage(): StatusMessage? = when (this) {
    LocationPermissionState.Granted -> null
    LocationPermissionState.NotRequested -> StatusMessage.PermissionNeeded
    LocationPermissionState.Denied -> StatusMessage.PermissionDenied
    LocationPermissionState.PermanentlyDenied -> StatusMessage.PermissionPermanentlyDenied
    LocationPermissionState.ApproximateOnly -> StatusMessage.ApproximateLocationOnly
}

private fun Double.roundToWholeMeters(): Int = Math.round(this).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

/** Rounded up, because accuracy is a worst case ("about 31 m" for 30.1). Null for NaN or infinity. */
internal fun Double.accuracyToWholeMetersOrNull(): Int? =
    takeIf { it.isFinite() }?.let { ceil(it).coerceIn(0.0, Int.MAX_VALUE.toDouble()).toInt() }
