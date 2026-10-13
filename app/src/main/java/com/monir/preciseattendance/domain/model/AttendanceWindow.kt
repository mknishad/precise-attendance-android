package com.monir.preciseattendance.domain.model

import java.time.Clock
import java.time.LocalTime

/**
 * The time of day during which attendance may be marked. Both ends are inclusive, and the window
 * must not cross midnight.
 *
 * The 09:00 to 10:30 default is an assumption taken from the design screenshot.
 */
data class AttendanceWindow(
    val start: LocalTime = DEFAULT_START,
    val end: LocalTime = DEFAULT_END,
) {
    init {
        require(!end.isBefore(start)) { "Window end $end is before start $start" }
    }

    operator fun contains(time: LocalTime): Boolean = !time.isBefore(start) && !time.isAfter(end)

    /** Whether the clock's current local time falls inside the window. */
    fun isOpen(clock: Clock): Boolean = LocalTime.now(clock) in this

    companion object {
        val DEFAULT_START: LocalTime = LocalTime.of(9, 0)
        val DEFAULT_END: LocalTime = LocalTime.of(10, 30)
    }
}
