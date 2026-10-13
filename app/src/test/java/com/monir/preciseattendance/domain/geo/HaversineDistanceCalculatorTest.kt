package com.monir.preciseattendance.domain.geo

import com.monir.preciseattendance.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HaversineDistanceCalculatorTest {

    private val calculator = HaversineDistanceCalculator()

    /** A point [meters] due north of [from]; along a meridian the haversine distance is exact. */
    private fun northOf(from: GeoPoint, meters: Double): GeoPoint {
        val degrees = Math.toDegrees(meters / HaversineDistanceCalculator.EARTH_RADIUS_METERS)
        return GeoPoint(from.latitude + degrees, from.longitude)
    }

    @Test
    fun samePoint_isZero() {
        val point = GeoPoint(-37.8, 145.0)
        assertEquals(0.0, calculator.metersBetween(point, point), 0.0)
    }

    @Test
    fun oneDegreeOfLatitude_isAboutOneHundredAndElevenKilometres() {
        val distance = calculator.metersBetween(GeoPoint(0.0, 0.0), GeoPoint(1.0, 0.0))
        assertEquals(111_195.0, distance, 1.0)
    }

    @Test
    fun oneDegreeOfLongitudeAtSixtyDegrees_isHalfAsLongAsAtTheEquator() {
        val distance = calculator.metersBetween(GeoPoint(60.0, 0.0), GeoPoint(60.0, 1.0))
        assertEquals(55_597.0, distance, 5.0)
    }

    @Test
    fun melbourneToSydney_isAboutSevenHundredAndThirteenKilometres() {
        val distance = calculator.metersBetween(GeoPoint(-37.8136, 144.9631), GeoPoint(-33.8688, 151.2093))
        assertEquals(713_400.0, distance, 1_000.0)
    }

    @Test
    fun londonToParis_isAboutThreeHundredAndFortyFourKilometres() {
        val distance = calculator.metersBetween(GeoPoint(51.5074, -0.1278), GeoPoint(48.8566, 2.3522))
        assertEquals(343_600.0, distance, 1_000.0)
    }

    @Test
    fun distanceIsSymmetric() {
        val a = GeoPoint(-37.8136, 144.9631)
        val b = GeoPoint(-33.8688, 151.2093)
        assertEquals(calculator.metersBetween(a, b), calculator.metersBetween(b, a), 1e-6)
    }

    @Test
    fun crossingTheAntimeridian_takesTheShortWay() {
        val distance = calculator.metersBetween(GeoPoint(0.0, 179.9999), GeoPoint(0.0, -179.9999))
        assertEquals(22.24, distance, 0.1)
    }

    @Test
    fun antipodalPoints_doNotProduceNaN() {
        val distance = calculator.metersBetween(GeoPoint(0.0, 0.0), GeoPoint(0.0, 180.0))
        assertEquals(Math.PI * HaversineDistanceCalculator.EARTH_RADIUS_METERS, distance, 1.0)
    }

    @Test
    fun pointJustInsideFiftyMetres_measuresBelowFifty() {
        val office = GeoPoint(-37.8, 145.0)
        val distance = calculator.metersBetween(office, northOf(office, 49.9))
        assertEquals(49.9, distance, 0.001)
        assertTrue(distance <= 50.0)
    }

    @Test
    fun pointJustOutsideFiftyMetres_measuresAboveFifty() {
        val office = GeoPoint(-37.8, 145.0)
        val distance = calculator.metersBetween(office, northOf(office, 50.1))
        assertEquals(50.1, distance, 0.001)
        assertTrue(distance > 50.0)
    }
}
