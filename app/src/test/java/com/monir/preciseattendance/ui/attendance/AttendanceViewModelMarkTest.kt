@file:OptIn(ExperimentalCoroutinesApi::class)

package com.monir.preciseattendance.ui.attendance

import app.cash.turbine.test
import com.monir.preciseattendance.MainDispatcherRule
import com.monir.preciseattendance.domain.geo.HaversineDistanceCalculator
import com.monir.preciseattendance.domain.geo.ProximityEvaluator
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.usecase.MarkAttendance
import com.monir.preciseattendance.domain.usecase.MarkAttendanceResult
import com.monir.preciseattendance.fakes.TEST_POINT
import com.monir.preciseattendance.fakes.pointNorthOf
import com.monir.preciseattendance.fakes.testFix
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AttendanceViewModelMarkTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixture = AttendanceViewModelFixture()

    private fun message(message: StatusMessage) = AttendanceUiEvent.ShowMessage(message)

    private fun returning(result: MarkAttendanceResult) {
        coEvery { fixture.markAttendance() } returns result
    }

    private suspend fun assertTapProduces(expected: AttendanceUiEvent) {
        val viewModel = fixture.createViewModel()
        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun success_emitsAttendanceMarked() = runTest {
        returning(MarkAttendanceResult.Success(markedAtMillis = 123L))

        assertTapProduces(AttendanceUiEvent.AttendanceMarked)
    }

    @Test
    fun everyRejection_mapsToItsOwnMessage() = runTest {
        val window = AttendanceWindow()
        val cases = listOf(
            MarkAttendanceResult.NoOfficeSet to message(StatusMessage.NoOfficeSet),
            MarkAttendanceResult.OutsideAttendanceWindow(window) to
                message(StatusMessage.OutsideWindow(window.start, window.end)),
            MarkAttendanceResult.WeakSignal(45.2) to message(StatusMessage.WeakSignal(46)),
            MarkAttendanceResult.OutOfRange(120.4) to message(StatusMessage.OutOfRange(120)),
            MarkAttendanceResult.LocationUnavailable(LocationError.PermissionDenied) to
                message(StatusMessage.PermissionNeeded),
            MarkAttendanceResult.LocationUnavailable(LocationError.ServicesDisabled) to
                AttendanceUiEvent.RequestLocationSettingsResolution,
            MarkAttendanceResult.LocationUnavailable(LocationError.Timeout) to
                message(StatusMessage.LocationTimedOut),
            MarkAttendanceResult.LocationUnavailable(LocationError.Unavailable) to
                message(StatusMessage.LocationRequestFailed),
            MarkAttendanceResult.LocationUnavailable(LocationError.ProviderFailure) to
                message(StatusMessage.LocationRequestFailed),
            MarkAttendanceResult.StorageFailure to message(StatusMessage.AttendanceSaveFailed),
        )
        for ((result, expected) in cases) {
            returning(result)

            assertTapProduces(expected)
        }
    }

    @Test
    fun staleEnabledState_isNotTrusted_whenTheFreshCheckFails() = runTest {
        // The live flow says "in range", so the button is enabled...
        val viewModel = fixture.createViewModel(markAttendance = realMarkAttendance())
        keepStateActive(viewModel)
        assertTrue(viewModel.state.value.isMarkAttendanceEnabled)

        // ...but the fresh fix taken at the moment of the tap is 120 m away.
        fixture.location.currentLocation =
            FixResult.Success(testFix(point = pointNorthOf(TEST_POINT, 120.0)))

        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
            assertEquals(message(StatusMessage.OutOfRange(120)), awaitItem())
            expectNoEvents()
        }
        assertTrue(fixture.attendance.recorded.isEmpty())
    }

    @Test
    fun staleEnabledState_isNotTrusted_whenTheWindowClosedSinceTheLastTick() = runTest {
        val viewModel = fixture.createViewModel(markAttendance = realMarkAttendance())
        keepStateActive(viewModel)
        assertTrue(viewModel.state.value.isMarkAttendanceEnabled)

        fixture.setTime("10:45:00") // no tick has run yet, so the state still says enabled

        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
            assertEquals(
                message(StatusMessage.OutsideWindow(AttendanceWindow().start, AttendanceWindow().end)),
                awaitItem(),
            )
        }
        assertTrue(fixture.attendance.recorded.isEmpty())
    }

    @Test
    fun withFreshChecksPassing_attendanceIsRecordedAndStateFlipsToMarked() = runTest {
        val viewModel = fixture.createViewModel(markAttendance = realMarkAttendance())
        keepStateActive(viewModel)

        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
            assertEquals(AttendanceUiEvent.AttendanceMarked, awaitItem())
        }
        runCurrent()

        assertEquals(listOf(fixture.clock.millis()), fixture.attendance.recorded)
        assertTrue(viewModel.state.value.alreadyMarkedToday)
        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun tappingWhenTheStateSaysDisabled_stillRunsTheFreshCheck() = runTest {
        returning(MarkAttendanceResult.OutOfRange(80.0))
        val viewModel = fixture.createViewModel(permission = LocationPermissionState.Denied)

        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
            assertEquals(message(StatusMessage.OutOfRange(80)), awaitItem())
        }
        coVerify(exactly = 1) { fixture.markAttendance() }
    }

    @Test
    fun whileMarking_showsLoading_andAnExtraTapIsIgnored() = runTest {
        val gate = CompletableDeferred<MarkAttendanceResult>()
        coEvery { fixture.markAttendance() } coAnswers { gate.await() }
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)

        viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
        viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked)
        runCurrent()
        assertTrue(viewModel.state.value.isMarkingAttendance)

        gate.complete(MarkAttendanceResult.Success(1L))
        runCurrent()

        assertFalse(viewModel.state.value.isMarkingAttendance)
        coVerify(exactly = 1) { fixture.markAttendance() }
    }

    private fun realMarkAttendance() = MarkAttendance(
        locationRepository = fixture.location,
        officeLocationRepository = fixture.offices,
        attendanceRepository = fixture.attendance,
        proximityEvaluator = ProximityEvaluator(HaversineDistanceCalculator()),
        window = AttendanceWindow(),
        clock = fixture.clock,
    )
}
