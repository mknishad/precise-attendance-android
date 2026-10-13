package com.monir.preciseattendance.data.repository

import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AttendanceRepositoryImpl @Inject constructor() : AttendanceRepository {

    override fun observeStatus(): Flow<AttendanceStatus> {
        TODO("Decide where attendance is recorded (local only, or a backend) and observe it")
    }

    override suspend fun markAttendance(at: GeoPoint): AttendanceStatus {
        TODO("Record the attendance and return the resulting status")
    }
}
