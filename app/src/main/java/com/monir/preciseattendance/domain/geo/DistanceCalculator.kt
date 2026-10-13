package com.monir.preciseattendance.domain.geo

import com.monir.preciseattendance.domain.model.GeoPoint

fun interface DistanceCalculator {
    /** Great-circle distance between [from] and [to], in metres. */
    fun metersBetween(from: GeoPoint, to: GeoPoint): Double
}
