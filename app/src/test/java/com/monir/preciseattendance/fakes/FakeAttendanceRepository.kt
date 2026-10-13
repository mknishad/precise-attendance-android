package com.monir.preciseattendance.fakes

import com.monir.preciseattendance.domain.repository.AttendanceRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAttendanceRepository : AttendanceRepository {
    val lastMarkedAtMillis = MutableStateFlow<Long?>(null)
    var recordFailure: IOException? = null
    val recorded = mutableListOf<Long>()

    override fun observeLastMarkedAtMillis(): Flow<Long?> = lastMarkedAtMillis

    override suspend fun recordAttendance(markedAtMillis: Long) {
        recordFailure?.let { throw it }
        recorded += markedAtMillis
        lastMarkedAtMillis.value = markedAtMillis
    }
}
