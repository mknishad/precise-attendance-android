package com.monir.preciseattendance.fakes

import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeOfficeLocationRepository(initial: OfficeLocation? = null) : OfficeLocationRepository {
    val office = MutableStateFlow(initial)
    var saveFailure: IOException? = null
    val saved = mutableListOf<OfficeLocation>()

    override fun observeOfficeLocation(): Flow<OfficeLocation?> = office

    override suspend fun saveOfficeLocation(office: OfficeLocation) {
        saveFailure?.let { throw it }
        saved += office
        this.office.value = office
    }
}
