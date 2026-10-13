package com.monir.preciseattendance.di

import com.monir.preciseattendance.domain.geo.DistanceCalculator
import com.monir.preciseattendance.domain.geo.HaversineDistanceCalculator
import com.monir.preciseattendance.domain.model.AttendanceWindow
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/** Time, distance and window rules, bound here so tests can replace each of them. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindDistanceCalculator(impl: HaversineDistanceCalculator): DistanceCalculator

    companion object {
        @Provides
        @Singleton
        fun provideClock(): Clock = DeviceZoneClock

        /** The 09:00 to 10:30 default is an assumption; change it in [AttendanceWindow]. */
        @Provides
        fun provideAttendanceWindow(): AttendanceWindow = AttendanceWindow()
    }
}
