package com.monir.preciseattendance.domain.geo

import com.monir.preciseattendance.domain.model.GeoPoint
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Spherical-earth distance. At the 50 m scale used here the model error is negligible. */
class HaversineDistanceCalculator @Inject constructor() : DistanceCalculator {

    override fun metersBetween(from: GeoPoint, to: GeoPoint): Double {
        val fromLat = Math.toRadians(from.latitude)
        val toLat = Math.toRadians(to.latitude)
        val deltaLat = toLat - fromLat
        val deltaLon = Math.toRadians(to.longitude - from.longitude)

        val a = sin(deltaLat / 2).squared() + cos(fromLat) * cos(toLat) * sin(deltaLon / 2).squared()
        // Rounding can push `a` a hair above 1 for near-antipodal points, which would make asin NaN.
        return 2 * EARTH_RADIUS_METERS * asin(min(1.0, sqrt(a)))
    }

    private fun Double.squared(): Double = this * this

    companion object {
        /** IUGG mean earth radius. */
        const val EARTH_RADIUS_METERS: Double = 6_371_008.8
    }
}
