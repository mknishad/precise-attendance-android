package com.monir.preciseattendance.fakes

import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.LocationFix
import com.monir.preciseattendance.domain.model.OfficeLocation
import java.time.Instant
import java.time.ZoneId

/** Arbitrary coordinates; the tests never depend on where in the world they are. */
val TEST_POINT = GeoPoint(latitude = -37.8, longitude = 145.0)

val TEST_ZONE: ZoneId = ZoneId.of("Australia/Melbourne")

fun testOffice(point: GeoPoint = TEST_POINT, savedAtMillis: Long = 1_000L) =
    OfficeLocation(point = point, savedAtMillis = savedAtMillis)

fun testFix(
    point: GeoPoint = TEST_POINT,
    accuracyMeters: Double = 5.0,
    timestampMillis: Long = 2_000L,
) = LocationFix(point = point, accuracyMeters = accuracyMeters, timestampMillis = timestampMillis)

/** A local date-time in [TEST_ZONE] as an instant, so tests can state times as the user sees them. */
fun melbourneInstant(date: String, time: String): Instant =
    java.time.LocalDateTime.parse("${date}T$time").atZone(TEST_ZONE).toInstant()
