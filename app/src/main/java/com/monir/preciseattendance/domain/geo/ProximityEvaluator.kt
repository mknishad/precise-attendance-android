package com.monir.preciseattendance.domain.geo

import com.monir.preciseattendance.domain.model.AttendancePolicy
import com.monir.preciseattendance.domain.model.LocationFix
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.domain.model.hasAcceptableAccuracy
import javax.inject.Inject

/** The single place that decides whether a fix counts as in range of the office. */
class ProximityEvaluator @Inject constructor(
    private val distanceCalculator: DistanceCalculator,
) {
    fun evaluate(office: OfficeLocation?, fix: LocationFix?): ProximityResult = when {
        office == null -> ProximityResult.NoOfficeSet
        fix == null -> ProximityResult.AcquiringFix
        !fix.hasAcceptableAccuracy() -> ProximityResult.WeakSignal(fix.accuracyMeters)
        else -> classify(distanceCalculator.metersBetween(office.point, fix.point))
    }

    private fun classify(distanceMeters: Double): ProximityResult =
        if (distanceMeters <= AttendancePolicy.ALLOWED_RADIUS_METERS) {
            ProximityResult.InRange(distanceMeters)
        } else {
            ProximityResult.OutOfRange(distanceMeters)
        }
}
