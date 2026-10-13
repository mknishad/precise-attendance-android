package com.monir.preciseattendance.domain.model

/** A single position reading with the accuracy radius the provider reported for it. */
data class LocationFix(
    val point: GeoPoint,
    val accuracyMeters: Double,
    val timestampMillis: Long,
)

/**
 * True only when the reported accuracy is a real number no worse than the allowed maximum.
 * Written as `<=` so a NaN accuracy is rejected rather than slipping through a `>` check.
 */
fun LocationFix.hasAcceptableAccuracy(): Boolean =
    accuracyMeters <= AttendancePolicy.MAX_ACCEPTABLE_ACCURACY_METERS
