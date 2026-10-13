package com.monir.preciseattendance.data.repository

import com.monir.preciseattendance.data.local.AttendanceStore
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val store: AttendanceStore,
) : AttendanceRepository {

    override fun observeLastMarkedAtMillis(): Flow<Long?> = store.observeLastMarkedAtMillis()

    override suspend fun recordAttendance(markedAtMillis: Long) =
        store.saveLastMarkedAtMillis(markedAtMillis)
}
