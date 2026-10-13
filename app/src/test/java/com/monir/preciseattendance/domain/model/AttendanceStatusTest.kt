package com.monir.preciseattendance.domain.model

import com.monir.preciseattendance.fakes.TEST_ZONE
import com.monir.preciseattendance.fakes.melbourneInstant
import java.time.Clock
import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceStatusTest {

    private val clock = Clock.fixed(melbourneInstant("2026-10-15", "09:30:00"), TEST_ZONE)

    @Test
    fun neverMarked_isNotMarked() {
        assertEquals(AttendanceStatus.NotMarked, attendanceStatusFor(null, clock))
    }

    @Test
    fun markedEarlierToday_isMarked() {
        val markedAt = melbourneInstant("2026-10-15", "00:00:01").toEpochMilli()
        assertEquals(AttendanceStatus.Marked(markedAt), attendanceStatusFor(markedAt, clock))
    }

    @Test
    fun markedYesterdayEvening_isNotMarked() {
        val markedAt = melbourneInstant("2026-10-14", "23:59:59").toEpochMilli()
        assertEquals(AttendanceStatus.NotMarked, attendanceStatusFor(markedAt, clock))
    }

    @Test
    fun theDayIsJudgedInTheClocksZone_notUtc() {
        // 00:30 local on the 15th is still the 14th in UTC, but it is "today" for the user.
        val markedAt = melbourneInstant("2026-10-15", "00:30:00").toEpochMilli()
        assertEquals(AttendanceStatus.Marked(markedAt), attendanceStatusFor(markedAt, clock))
    }
}
