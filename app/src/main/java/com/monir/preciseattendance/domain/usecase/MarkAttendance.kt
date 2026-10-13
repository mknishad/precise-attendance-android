package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import com.monir.preciseattendance.domain.repository.LocationRepository
import javax.inject.Inject

class MarkAttendance @Inject constructor(
    private val locationRepository: LocationRepository,
    private val attendanceRepository: AttendanceRepository,
) {
    /** Marks attendance at the current location, provided the user is within range of the office. */
    suspend operator fun invoke(): AttendanceStatus {
        TODO("Re-check range against the office point, then call markAttendance()")
    }
}
