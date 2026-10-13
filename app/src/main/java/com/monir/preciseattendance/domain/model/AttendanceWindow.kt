package com.monir.preciseattendance.domain.model

import java.time.Clock
import java.time.Duration
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

    /**
     * Milliseconds from [now] until the window next opens or closes today, or null if neither is
     * still to come. The close is reported just after [end], the first instant outside the window.
     */
    fun millisUntilNextChange(now: LocalTime): Long? {
        val untilOpen = Duration.between(now, start)
        val untilClose = Duration.between(now, end).plusMillis(1)
        return listOf(untilOpen, untilClose)
            .filter { !it.isNegative && !it.isZero }
            .minOrNull()
            ?.toMillis()
    }

    companion object {
        val DEFAULT_START: LocalTime = LocalTime.of(9, 0)
        val DEFAULT_END: LocalTime = LocalTime.of(23, 30)
    }
}
