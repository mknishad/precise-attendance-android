package com.monir.preciseattendance.domain.model

/** The business thresholds for marking attendance. Change them here and nowhere else. */
object AttendancePolicy {
    /** Furthest distance from the office point at which attendance can be marked (inclusive). */
    const val ALLOWED_RADIUS_METERS: Double = 50.0

    /** Worst horizontal accuracy a fix may report and still be trusted (inclusive). */
    const val MAX_ACCEPTABLE_ACCURACY_METERS: Double = 30.0
}
