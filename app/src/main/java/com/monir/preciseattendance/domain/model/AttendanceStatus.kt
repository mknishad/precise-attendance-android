package com.monir.preciseattendance.domain.model

import java.time.Clock
import java.time.Instant

sealed interface AttendanceStatus {
    data object NotMarked : AttendanceStatus
    data class Marked(val markedAtEpochMillis: Long) : AttendanceStatus
}

/**
 * Derives today's status from the last recorded timestamp: marked only if that moment falls on
 * the same calendar day as [clock] now, in the clock's zone.
 */
fun attendanceStatusFor(lastMarkedAtMillis: Long?, clock: Clock): AttendanceStatus {
    if (lastMarkedAtMillis == null) return AttendanceStatus.NotMarked
    val markedDay = Instant.ofEpochMilli(lastMarkedAtMillis).atZone(clock.zone).toLocalDate()
    return if (markedDay == clock.instant().atZone(clock.zone).toLocalDate()) {
        AttendanceStatus.Marked(lastMarkedAtMillis)
    } else {
        AttendanceStatus.NotMarked
    }
}
