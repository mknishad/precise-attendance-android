package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.fakes.FakeLocationRepository
import com.monir.preciseattendance.fakes.FakeOfficeLocationRepository
import com.monir.preciseattendance.fakes.TEST_ZONE
import com.monir.preciseattendance.fakes.melbourneInstant
import com.monir.preciseattendance.fakes.testFix
import java.io.IOException
import java.time.Clock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetOfficeLocationTest {

    private val now = melbourneInstant("2026-10-15", "09:00:00")
    private val clock = Clock.fixed(now, TEST_ZONE)
    private val location = FakeLocationRepository()
    private val offices = FakeOfficeLocationRepository()
    private val setOfficeLocation = SetOfficeLocation(location, offices, clock)

    @Test
    fun goodFix_isSavedWithTheCurrentTime() = runTest {
        val point = GeoPoint(-37.81362765432101, 144.96305761234567)
        location.currentLocation = FixResult.Success(testFix(point = point, accuracyMeters = 8.0))

        val result = setOfficeLocation()

        val expected = OfficeLocation(point = point, savedAtMillis = now.toEpochMilli())
        assertEquals(SetOfficeLocationResult.Success(expected), result)
        assertEquals(listOf(expected), offices.saved)
    }

    @Test
    fun accuracyExactlyAtTheLimit_isAccepted() = runTest {
        location.currentLocation = FixResult.Success(testFix(accuracyMeters = 30.0))

        assertTrue(setOfficeLocation() is SetOfficeLocationResult.Success)
    }

    @Test
    fun accuracyWorseThanTheLimit_isRejectedAndNothingIsSaved() = runTest {
        location.currentLocation = FixResult.Success(testFix(accuracyMeters = 30.1))

        val result = setOfficeLocation()

        assertEquals(SetOfficeLocationResult.WeakSignal(30.1), result)
        assertTrue(offices.saved.isEmpty())
    }

    @Test
    fun nanAccuracy_isRejected() = runTest {
        location.currentLocation = FixResult.Success(testFix(accuracyMeters = Double.NaN))

        assertTrue(setOfficeLocation() is SetOfficeLocationResult.WeakSignal)
        assertTrue(offices.saved.isEmpty())
    }

    @Test
    fun everyLocationError_isReportedAndNothingIsSaved() = runTest {
        val errors = listOf(
            LocationError.PermissionDenied,
            LocationError.ServicesDisabled,
            LocationError.Timeout,
            LocationError.Unavailable,
            LocationError.ProviderFailure,
        )
        for (error in errors) {
            location.currentLocation = FixResult.Failure(error)

            assertEquals(SetOfficeLocationResult.LocationUnavailable(error), setOfficeLocation())
        }
        assertTrue(offices.saved.isEmpty())
    }

    @Test
    fun writeFailure_isReportedAsStorageFailure() = runTest {
        offices.saveFailure = IOException("disk full")

        assertEquals(SetOfficeLocationResult.StorageFailure, setOfficeLocation())
    }
}
