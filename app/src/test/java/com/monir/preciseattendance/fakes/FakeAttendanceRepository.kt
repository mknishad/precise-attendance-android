package com.monir.preciseattendance.fakes

import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAttendanceRepository : AttendanceRepository {
    private val status = MutableStateFlow<AttendanceStatus>(AttendanceStatus.NotMarked)
    var recordFailure: IOException? = null
    val recorded = mutableListOf<Long>()

    override fun observeStatus(): Flow<AttendanceStatus> = status

    override suspend fun recordAttendance(markedAtMillis: Long) {
        recordFailure?.let { throw it }
        recorded += markedAtMillis
        status.value = AttendanceStatus.Marked(markedAtMillis)
    }
}
