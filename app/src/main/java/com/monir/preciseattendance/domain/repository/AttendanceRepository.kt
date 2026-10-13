package com.monir.preciseattendance.domain.repository

import com.monir.preciseattendance.domain.model.AttendanceStatus
import java.io.IOException
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    /** Emits whether attendance has been marked today. */
    fun observeStatus(): Flow<AttendanceStatus>

    /** @throws IOException if the timestamp cannot be written. */
    suspend fun recordAttendance(markedAtMillis: Long)
}
