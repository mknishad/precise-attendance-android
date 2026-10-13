package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.ui.attendance.components.OFFICE_ZOOM
import com.monir.preciseattendance.ui.attendance.components.zoomToShowDistance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapZoomTest {

    private val newYork = 40.7128

    @Test
    fun noDistance_staysAtTheOfficeZoom() {
        assertEquals(OFFICE_ZOOM, zoomToShowDistance(null, newYork), 0f)
        assertEquals(OFFICE_ZOOM, zoomToShowDistance(0, newYork), 0f)
    }

    @Test
    fun veryClose_isCappedAtTheOfficeZoom() {
        assertEquals(OFFICE_ZOOM, zoomToShowDistance(1, newYork), 0f)
    }

    @Test
    fun zoomsOutInWholeSteps_asTheUserMovesAway() {
        // Expected values were worked out separately from the formula in the code.
        assertEquals(16f, zoomToShowDistance(60, newYork), 0f)
        assertEquals(15f, zoomToShowDistance(120, newYork), 0f)
        assertEquals(13f, zoomToShowDistance(300, newYork), 0f)
    }

    @Test
    fun farAway_isClampedSoTheMapStaysUseful() {
        assertEquals(13f, zoomToShowDistance(2_000, newYork), 0f)
        assertEquals(13f, zoomToShowDistance(Int.MAX_VALUE, newYork), 0f)
    }

    @Test
    fun neverZoomsInAsTheDistanceGrows() {
        val zooms = (1..500 step 7).map { zoomToShowDistance(it, newYork) }
        assertTrue(zooms.zipWithNext().all { (nearer, farther) -> farther <= nearer })
    }

    @Test
    fun nearTheEquatorAMetreCoversMoreGround_soItZoomsInFurther() {
        assertEquals(17f, zoomToShowDistance(37, 0.0), 0f)
        assertEquals(16f, zoomToShowDistance(37, newYork), 0f)
    }

    @Test
    fun atThePole_theResultStaysFinite() {
        val zoom = zoomToShowDistance(100, 90.0)
        assertTrue(zoom in 13f..18f)
    }
}
