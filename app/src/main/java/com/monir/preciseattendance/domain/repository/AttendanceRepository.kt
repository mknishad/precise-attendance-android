package com.monir.preciseattendance.domain.repository

import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun observeStatus(): Flow<AttendanceStatus>

    suspend fun markAttendance(at: GeoPoint): AttendanceStatus
}
