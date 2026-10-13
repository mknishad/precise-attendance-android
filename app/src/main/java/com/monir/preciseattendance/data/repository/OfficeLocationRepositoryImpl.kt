package com.monir.preciseattendance.data.repository

import com.monir.preciseattendance.data.local.OfficeLocationStore
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class OfficeLocationRepositoryImpl @Inject constructor(
    private val store: OfficeLocationStore,
) : OfficeLocationRepository {

    override fun observeOfficeLocation(): Flow<OfficeLocation?> = store.observe()

    override suspend fun saveOfficeLocation(office: OfficeLocation) = store.save(office)
}
