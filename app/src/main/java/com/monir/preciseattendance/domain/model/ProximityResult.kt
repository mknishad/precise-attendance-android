package com.monir.preciseattendance.domain.model

/** Where the user stands relative to the office, with the reliability of that answer. */
sealed interface ProximityResult {
    data object NoOfficeSet : ProximityResult

    /** An office is set but no fix has arrived yet. */
    data object AcquiringFix : ProximityResult

    /** The latest fix is too inaccurate to trust, so it never counts as in or out of range. */
    data class WeakSignal(val accuracyMeters: Double) : ProximityResult

    data class InRange(val distanceMeters: Double) : ProximityResult
    data class OutOfRange(val distanceMeters: Double) : ProximityResult

    /** Location updates stopped because of [error], for example permission revoked mid-session. */
    data class LocationUnavailable(val error: LocationError) : ProximityResult
}
