package com.monir.preciseattendance.di

import com.monir.preciseattendance.data.location.LocationRepositoryImpl
import com.monir.preciseattendance.data.repository.AttendanceRepositoryImpl
import com.monir.preciseattendance.data.repository.OfficeLocationRepositoryImpl
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindOfficeLocationRepository(impl: OfficeLocationRepositoryImpl): OfficeLocationRepository

    @Binds
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository
}
