package com.monir.preciseattendance.domain.repository

import java.io.IOException
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    /**
     * Emits the time attendance was last marked, or null if never. Callers derive "marked today"
     * from this with [com.monir.preciseattendance.domain.model.attendanceStatusFor], so the
     * answer can be re-evaluated as the day changes.
     */
    fun observeLastMarkedAtMillis(): Flow<Long?>

    /** @throws IOException if the timestamp cannot be written. */
    suspend fun recordAttendance(markedAtMillis: Long)
}
