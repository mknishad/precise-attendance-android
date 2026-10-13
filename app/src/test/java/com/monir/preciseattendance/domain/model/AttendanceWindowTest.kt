package com.monir.preciseattendance.domain.model

import com.monir.preciseattendance.fakes.TEST_ZONE
import com.monir.preciseattendance.fakes.melbourneInstant
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceWindowTest {

    private val window = AttendanceWindow()

    @Test
    fun defaultsToNineUntilTenThirty() {
        assertEquals(LocalTime.of(9, 0), window.start)
        assertEquals(LocalTime.of(10, 30), window.end)
    }

    @Test
    fun startBoundary_isInside() {
        assertTrue(LocalTime.of(9, 0) in window)
    }

    @Test
    fun endBoundary_isInside() {
        assertTrue(LocalTime.of(10, 30) in window)
    }

    @Test
    fun oneNanosecondBeforeStart_isOutside() {
        assertFalse(LocalTime.of(9, 0).minusNanos(1) in window)
    }

    @Test
    fun oneNanosecondAfterEnd_isOutside() {
        assertFalse(LocalTime.of(10, 30).plusNanos(1) in window)
    }

    @Test
    fun middleOfTheWindow_isInside() {
        assertTrue(LocalTime.of(9, 45) in window)
    }

    @Test
    fun clearlyOutsideTheWindow_isOutside() {
        assertFalse(LocalTime.of(8, 0) in window)
        assertFalse(LocalTime.of(14, 0) in window)
        assertFalse(LocalTime.MIDNIGHT in window)
    }

    @Test
    fun customWindow_isHonoured() {
        val custom = AttendanceWindow(start = LocalTime.of(7, 30), end = LocalTime.of(8, 0))
        assertTrue(LocalTime.of(7, 45) in custom)
        assertFalse(LocalTime.of(9, 15) in custom)
    }

    @Test
    fun endBeforeStart_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            AttendanceWindow(start = LocalTime.of(10, 0), end = LocalTime.of(9, 0))
        }
    }

    @Test
    fun isOpen_readsLocalTimeInTheClocksZone() {
        // 23:30 UTC on 14 October is 10:30 on 15 October in Melbourne (AEDT, UTC+11).
        val instant = melbourneInstant("2026-10-15", "10:30:00")
        assertTrue(window.isOpen(Clock.fixed(instant, TEST_ZONE)))
        assertFalse(window.isOpen(Clock.fixed(instant, ZoneOffset.UTC)))
    }
}
