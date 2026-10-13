package com.monir.preciseattendance.ui.attendance.components

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln

internal const val OFFICE_ZOOM = 18f
private const val MIN_FIT_ZOOM = 13f

/** Metres per density-independent pixel at zoom 0 on the equator (Web Mercator, 256 dp tiles). */
private const val METERS_PER_DP_AT_ZOOM_ZERO = 156_543.03392

/**
 * The smallest visible radius around the office, in dp. The map is short and the coordinate pill
 * covers its upper part, so this is deliberately conservative.
 */
private const val VISIBLE_RADIUS_DP = 44.0

/** Extra room so the dot sits inside the view rather than on its edge. */
private const val MARGIN_FACTOR = 1.2

/**
 * The most zoomed-in level, capped at [OFFICE_ZOOM], at which a point [distanceMeters] from the
 * office is still inside the visible radius. Whole zoom levels only, so the camera moves when the
 * level changes rather than on every location update. With no distance it is [OFFICE_ZOOM].
 */
internal fun zoomToShowDistance(distanceMeters: Int?, officeLatitude: Double): Float {
    if (distanceMeters == null || distanceMeters <= 0) return OFFICE_ZOOM
    val metersPerDpAtZoomZero = METERS_PER_DP_AT_ZOOM_ZERO * cos(Math.toRadians(officeLatitude)).coerceAtLeast(MIN_COSINE)
    val fitting = metersPerDpAtZoomZero * VISIBLE_RADIUS_DP / (MARGIN_FACTOR * distanceMeters)
    return floor(ln(fitting) / ln(2.0)).toFloat().coerceIn(MIN_FIT_ZOOM, OFFICE_ZOOM)
}

/** Keeps the zoom finite near the poles, where the cosine of the latitude approaches zero. */
private const val MIN_COSINE = 0.01
