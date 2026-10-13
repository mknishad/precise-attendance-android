package com.monir.preciseattendance.data.location

import android.location.Location
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.LocationFix

/**
 * Maps a platform [Location] to a domain fix. A location that reports no accuracy cannot be
 * trusted for a 50 m check, so it is dropped (null) rather than given an invented accuracy.
 */
fun Location.toLocationFix(): LocationFix? =
    if (hasAccuracy()) {
        LocationFix(
            point = GeoPoint(latitude, longitude),
            accuracyMeters = accuracy.toDouble(),
            timestampMillis = time,
        )
    } else {
        null
    }
