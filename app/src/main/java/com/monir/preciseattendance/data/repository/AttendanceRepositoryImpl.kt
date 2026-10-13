package com.monir.preciseattendance.data.repository

import com.monir.preciseattendance.data.local.AttendanceStore
import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.model.attendanceStatusFor
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val store: AttendanceStore,
    private val clock: Clock,
) : AttendanceRepository {

    /** Evaluated when the stored value changes, so it does not roll over at midnight by itself. */
    override fun observeStatus(): Flow<AttendanceStatus> =
        store.observeLastMarkedAtMillis().map { lastMarkedAt -> attendanceStatusFor(lastMarkedAt, clock) }

    override suspend fun recordAttendance(markedAtMillis: Long) =
        store.saveLastMarkedAtMillis(markedAtMillis)
}
