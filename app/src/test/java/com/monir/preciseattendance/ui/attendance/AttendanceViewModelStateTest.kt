@file:OptIn(ExperimentalCoroutinesApi::class)

package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.MainDispatcherRule
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.fakes.melbourneInstant
import com.monir.preciseattendance.fakes.testOffice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AttendanceViewModelStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixture = AttendanceViewModelFixture()

    @Test
    fun beforeAnythingIsCollected_stateIsTheSafeDefault() = runTest {
        val viewModel = fixture.createViewModel(permission = null)

        val state = viewModel.state.value

        assertEquals(LocationPermissionState.NotRequested, state.permissionState)
        assertFalse(state.isMarkAttendanceEnabled)
    }

    @Test
    fun noPermission_noOffice_gpsOff_isDisabledAndAsksForPermissionFirst() = runTest {
        fixture.offices.office.value = null
        fixture.location.locationEnabled.value = false
        val viewModel = fixture.createViewModel(permission = LocationPermissionState.NotRequested)

        keepStateActive(viewModel)

        val state = viewModel.state.value
        assertEquals(LocationPermissionState.NotRequested, state.permissionState)
        assertFalse(state.isLocationEnabled)
        assertNull(state.officeLocation)
        assertFalse(state.isMarkAttendanceEnabled)
        assertEquals(StatusMessage.PermissionNeeded, state.statusMessage)
        assertEquals(0, fixture.proximityCollectors)
    }

    @Test
    fun state_exposesTheOfficeAndTheLiveDistance() = runTest {
        fixture.proximity.value = ProximityResult.InRange(50.0)
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        val state = viewModel.state.value
        assertEquals(testOffice(), state.officeLocation)
        assertEquals(50.0, state.distanceMeters!!, 0.0)
        assertEquals(0.75f, state.distanceProgress, 0.0001f)
        assertEquals(StatusMessage.InRange(50), state.statusMessage)
    }

    @Test
    fun grantingPermission_startsLocationCollection_andRevokingStopsIt() = runTest {
        val viewModel = fixture.createViewModel(permission = LocationPermissionState.NotRequested)
        keepStateActive(viewModel)
        assertEquals(0, fixture.proximityCollectors)

        viewModel.onIntent(AttendanceIntent.PermissionResult(LocationPermissionState.Granted))
        runCurrent()
        assertEquals(1, fixture.proximityCollectors)

        viewModel.onIntent(AttendanceIntent.PermissionResult(LocationPermissionState.Denied))
        runCurrent()
        assertEquals(0, fixture.proximityCollectors)

        viewModel.onIntent(AttendanceIntent.PermissionResult(LocationPermissionState.Granted))
        runCurrent()
        assertEquals(1, fixture.proximityCollectors)
    }

    @Test
    fun approximateOnlyAndPermanentDenial_neverStartLocationCollection() = runTest {
        val viewModel = fixture.createViewModel(permission = LocationPermissionState.ApproximateOnly)
        keepStateActive(viewModel)
        assertEquals(0, fixture.proximityCollectors)
        assertEquals(StatusMessage.ApproximateLocationOnly, viewModel.state.value.statusMessage)

        viewModel.onIntent(AttendanceIntent.PermissionResult(LocationPermissionState.PermanentlyDenied))
        runCurrent()
        assertEquals(0, fixture.proximityCollectors)
        assertEquals(StatusMessage.PermissionPermanentlyDenied, viewModel.state.value.statusMessage)
    }

    @Test
    fun locationServicesOff_stopsLocationCollection_andOnRestartsIt() = runTest {
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)
        assertEquals(1, fixture.proximityCollectors)

        fixture.location.locationEnabled.value = false
        runCurrent()
        assertEquals(0, fixture.proximityCollectors)
        assertEquals(
            ProximityResult.LocationUnavailable(LocationError.ServicesDisabled),
            viewModel.state.value.proximity,
        )
        assertEquals(StatusMessage.LocationOff, viewModel.state.value.statusMessage)

        fixture.location.locationEnabled.value = true
        runCurrent()
        assertEquals(1, fixture.proximityCollectors)
    }

    @Test
    fun locationCollection_stopsWhenNothingIsSubscribedAnymore() = runTest {
        val viewModel = fixture.createViewModel()
        val subscription = backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertEquals(1, fixture.proximityCollectors)

        subscription.cancel()
        advanceTimeBy(5_001)
        runCurrent()

        assertEquals(0, fixture.proximityCollectors)
    }

    @Test
    fun markAttendanceIsEnabled_onlyWhenEverythingIsSatisfied() = runTest {
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        assertTrue(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun markAttendanceIsDisabled_forEveryPermissionStateExceptGranted() = runTest {
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)

        for (permission in LocationPermissionState.entries - LocationPermissionState.Granted) {
            viewModel.onIntent(AttendanceIntent.PermissionResult(permission))
            runCurrent()
            assertFalse("$permission", viewModel.state.value.isMarkAttendanceEnabled)
        }
    }

    @Test
    fun markAttendanceIsDisabled_whenGpsIsOff() = runTest {
        fixture.location.locationEnabled.value = false
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun markAttendanceIsDisabled_whenOutOfRange() = runTest {
        fixture.proximity.value = ProximityResult.OutOfRange(50.1)
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)
        assertEquals(StatusMessage.OutOfRange(51), viewModel.state.value.statusMessage)
    }

    @Test
    fun markAttendanceIsDisabled_whenSignalIsWeak() = runTest {
        fixture.proximity.value = ProximityResult.WeakSignal(45.2)
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)
        assertEquals(StatusMessage.WeakSignal(46), viewModel.state.value.statusMessage)
    }

    @Test
    fun markAttendanceIsDisabled_whileAcquiringAFix_orWithNoOffice() = runTest {
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)

        fixture.proximity.value = ProximityResult.AcquiringFix
        runCurrent()
        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)

        fixture.proximity.value = ProximityResult.NoOfficeSet
        runCurrent()
        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun markAttendanceIsDisabled_outsideTheWindow() = runTest {
        fixture.setTime("10:30:01")
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        val state = viewModel.state.value
        assertFalse(state.isWithinWindow)
        assertFalse(state.isMarkAttendanceEnabled)
        assertTrue(state.statusMessage is StatusMessage.OutsideWindow)
    }

    @Test
    fun markAttendanceIsDisabled_whenAlreadyMarkedToday() = runTest {
        fixture.attendance.lastMarkedAtMillis.value = melbourneInstant("2026-10-15", "09:05:00").toEpochMilli()
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        val state = viewModel.state.value
        assertTrue(state.alreadyMarkedToday)
        assertFalse(state.isMarkAttendanceEnabled)
        assertEquals(StatusMessage.AlreadyMarkedToday, state.statusMessage)
    }

    @Test
    fun attendanceMarkedYesterday_doesNotCountAsToday() = runTest {
        fixture.attendance.lastMarkedAtMillis.value = melbourneInstant("2026-10-14", "09:05:00").toEpochMilli()
        val viewModel = fixture.createViewModel()

        keepStateActive(viewModel)

        assertFalse(viewModel.state.value.alreadyMarkedToday)
        assertTrue(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun windowOpening_isNoticedWhenTheClockCrossesTheStartBoundary() = runTest {
        fixture.setTime("08:59:30")
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)
        assertFalse(viewModel.state.value.isWithinWindow)

        fixture.setTime("09:00:00")
        advanceTimeBy(30_000)
        runCurrent()

        assertTrue(viewModel.state.value.isWithinWindow)
        assertTrue(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun windowClosing_isNoticedJustAfterTheEndBoundary_notAMinuteLater() = runTest {
        fixture.clock.now = melbourneInstant("2026-10-15", "10:29:59").plusMillis(500)
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)
        assertTrue(viewModel.state.value.isMarkAttendanceEnabled)

        fixture.clock.now = melbourneInstant("2026-10-15", "10:30:00").plusMillis(1)
        advanceTimeBy(501)
        runCurrent()

        assertFalse(viewModel.state.value.isWithinWindow)
        assertFalse(viewModel.state.value.isMarkAttendanceEnabled)
    }

    @Test
    fun windowIsAlsoReEvaluatedEveryMinute() = runTest {
        fixture.setTime("08:00:00")
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)
        assertFalse(viewModel.state.value.isWithinWindow)

        fixture.setTime("09:05:00")
        advanceTimeBy(60_000)
        runCurrent()

        assertTrue(viewModel.state.value.isWithinWindow)
    }

    @Test
    fun alreadyMarkedToday_resetsWhenTheDayChanges() = runTest {
        fixture.setTime("23:59:30", date = "2026-10-14")
        fixture.attendance.lastMarkedAtMillis.value = melbourneInstant("2026-10-14", "09:10:00").toEpochMilli()
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)
        assertTrue(viewModel.state.value.alreadyMarkedToday)

        fixture.setTime("00:00:30", date = "2026-10-15")
        advanceTimeBy(60_000)
        runCurrent()

        assertFalse(viewModel.state.value.alreadyMarkedToday)
    }
}
