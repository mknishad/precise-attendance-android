package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.geo.DistanceCalculator
import com.monir.preciseattendance.domain.geo.ProximityEvaluator
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import com.monir.preciseattendance.fakes.FakeAttendanceRepository
import com.monir.preciseattendance.fakes.FakeLocationRepository
import com.monir.preciseattendance.fakes.FakeOfficeLocationRepository
import com.monir.preciseattendance.fakes.MutableTestClock
import com.monir.preciseattendance.fakes.melbourneInstant
import com.monir.preciseattendance.fakes.testFix
import com.monir.preciseattendance.fakes.testOffice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkAttendanceTest {

    private val clock = MutableTestClock(melbourneInstant("2026-10-15", "09:30:00"))
    private val location = FakeLocationRepository()
    private val offices = FakeOfficeLocationRepository(initial = testOffice())
    private val attendance = FakeAttendanceRepository()
    private var distanceMeters = 10.0
    private val window = AttendanceWindow()

    private fun markAttendance(
        repository: AttendanceRepository = attendance,
    ) = MarkAttendance(
        locationRepository = location,
        officeLocationRepository = offices,
        attendanceRepository = repository,
        proximityEvaluator = ProximityEvaluator(DistanceCalculator { _, _ -> distanceMeters }),
        window = window,
        clock = clock,
    )

    private fun setTime(time: String) {
        clock.now = melbourneInstant("2026-10-15", time)
    }

    @Test
    fun inRangeInsideWindow_recordsTheTimestampAndSucceeds() = runTest {
        val result = markAttendance()()

        val expected = clock.millis()
        assertEquals(MarkAttendanceResult.Success(expected), result)
        assertEquals(listOf(expected), attendance.recorded)
    }

    @Test
    fun exactlyFiftyMetres_succeeds() = runTest {
        distanceMeters = 50.0

        assertTrue(markAttendance()() is MarkAttendanceResult.Success)
    }

    @Test
    fun windowStartAndEndBoundaries_succeed() = runTest {
        setTime("09:00:00")
        assertTrue(markAttendance()() is MarkAttendanceResult.Success)

        setTime("10:30:00")
        assertTrue(markAttendance()() is MarkAttendanceResult.Success)
    }

    @Test
    fun noOffice_isRejectedWithoutTouchingTheGps() = runTest {
        offices.office.value = null

        assertEquals(MarkAttendanceResult.NoOfficeSet, markAttendance()())
        assertEquals(0, location.getCurrentLocationCalls)
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun beforeTheWindow_isRejectedWithoutTouchingTheGps() = runTest {
        setTime("08:59:59")

        assertEquals(MarkAttendanceResult.OutsideAttendanceWindow(window), markAttendance()())
        assertEquals(0, location.getCurrentLocationCalls)
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun afterTheWindow_isRejected() = runTest {
        setTime("10:30:01")

        assertEquals(MarkAttendanceResult.OutsideAttendanceWindow(window), markAttendance()())
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun windowClosingWhileTheFixIsBeingFetched_isRejected() = runTest {
        setTime("10:29:59")
        location.onGetCurrentLocation = { setTime("10:30:05") }

        assertEquals(MarkAttendanceResult.OutsideAttendanceWindow(window), markAttendance()())
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun outOfRange_isRejectedWithTheDistance() = runTest {
        distanceMeters = 50.1

        assertEquals(MarkAttendanceResult.OutOfRange(50.1), markAttendance()())
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun weakFix_isRejected_evenWhenItLooksInRange() = runTest {
        distanceMeters = 0.0
        location.currentLocation = FixResult.Success(testFix(accuracyMeters = 45.0))

        assertEquals(MarkAttendanceResult.WeakSignal(45.0), markAttendance()())
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun everyLocationError_isReported() = runTest {
        val errors = listOf(
            LocationError.PermissionDenied,
            LocationError.ServicesDisabled,
            LocationError.Timeout,
            LocationError.Unavailable,
            LocationError.ProviderFailure,
        )
        for (error in errors) {
            location.currentLocation = FixResult.Failure(error)

            assertEquals(MarkAttendanceResult.LocationUnavailable(error), markAttendance()())
        }
        assertTrue(attendance.recorded.isEmpty())
    }

    @Test
    fun usesAFreshFixOnEveryTap() = runTest {
        val useCase = markAttendance()

        useCase()
        useCase()

        assertEquals(2, location.getCurrentLocationCalls)
    }

    @Test
    fun writeFailure_isReportedAsStorageFailure() = runTest {
        val failing = mockk<AttendanceRepository>()
        coEvery { failing.recordAttendance(any()) } throws IOException("disk full")

        assertEquals(MarkAttendanceResult.StorageFailure, markAttendance(failing)())
        coVerify(exactly = 1) { failing.recordAttendance(any()) }
    }
}
