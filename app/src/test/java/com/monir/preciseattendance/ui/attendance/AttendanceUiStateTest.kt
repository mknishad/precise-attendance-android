package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.ProximityResult
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceUiStateTest {

    private val ready = AttendanceUiState(
        permissionState = LocationPermissionState.Granted,
        isLocationEnabled = true,
        proximity = ProximityResult.InRange(10.0),
        isWithinWindow = true,
        alreadyMarkedToday = false,
    )

    @Test
    fun progress_isFullAtZeroMetres() {
        assertEquals(1f, distanceProgressFor(0.0), 0f)
    }

    @Test
    fun progress_isEmptyAtExactlyTheMaximum() {
        assertEquals(DISTANCE_RING_MAX_METERS, 200.0, 0.0)
        assertEquals(0f, distanceProgressFor(200.0), 0f)
    }

    @Test
    fun progress_isEmptyBeyondTheMaximum_notNegative() {
        assertEquals(0f, distanceProgressFor(200.1), 0f)
        assertEquals(0f, distanceProgressFor(5_000.0), 0f)
        assertEquals(0f, distanceProgressFor(Double.POSITIVE_INFINITY), 0f)
    }

    @Test
    fun progress_scalesLinearlyBetween() {
        assertEquals(0.5f, distanceProgressFor(100.0), 0.0001f)
        assertEquals(0.75f, distanceProgressFor(50.0), 0.0001f)
    }

    @Test
    fun progress_isClampedToFull_forANegativeDistance() {
        assertEquals(1f, distanceProgressFor(-3.0), 0f)
    }

    @Test
    fun progress_isEmptyWithNoDistance() {
        assertEquals(0f, distanceProgressFor(null), 0f)
        assertEquals(0f, distanceProgressFor(Double.NaN), 0f)
    }

    @Test
    fun distanceAndProgress_comeFromTheProximityResult() {
        assertEquals(10.0, ready.distanceMeters!!, 0.0)
        assertEquals(0.95f, ready.distanceProgress, 0.0001f)

        val out = ready.copy(proximity = ProximityResult.OutOfRange(250.0))
        assertEquals(250.0, out.distanceMeters!!, 0.0)
        assertEquals(0f, out.distanceProgress, 0f)
    }

    @Test
    fun noDistance_whenTheMeasurementCannotBeTrusted() {
        assertNull(ready.copy(proximity = ProximityResult.WeakSignal(80.0)).distanceMeters)
        assertNull(ready.copy(proximity = ProximityResult.AcquiringFix).distanceMeters)
        assertNull(ready.copy(proximity = ProximityResult.OutOfRange(Double.NaN)).distanceMeters)
    }

    @Test
    fun markEnabled_whenAllFiveConditionsHold() {
        assertTrue(ready.isMarkAttendanceEnabled)
    }

    @Test
    fun markEnabled_isFalseWhenAnySingleConditionFails() {
        assertFalse(ready.copy(permissionState = LocationPermissionState.ApproximateOnly).isMarkAttendanceEnabled)
        assertFalse(ready.copy(isLocationEnabled = false).isMarkAttendanceEnabled)
        assertFalse(ready.copy(proximity = ProximityResult.OutOfRange(60.0)).isMarkAttendanceEnabled)
        assertFalse(ready.copy(isWithinWindow = false).isMarkAttendanceEnabled)
        assertFalse(ready.copy(alreadyMarkedToday = true).isMarkAttendanceEnabled)
    }

    @Test
    fun markEnabled_ignoresUnrelatedFlags() {
        assertTrue(ready.copy(isSettingOffice = true).isMarkAttendanceEnabled)
    }

    @Test
    fun statusMessage_putsPermissionAndLocationBeforeEverythingElse() {
        val badEverything = ready.copy(
            permissionState = LocationPermissionState.PermanentlyDenied,
            isLocationEnabled = false,
        )
        assertEquals(StatusMessage.PermissionPermanentlyDenied, badEverything.statusMessage)
        assertEquals(StatusMessage.LocationOff, badEverything.copy(permissionState = LocationPermissionState.Granted).statusMessage)
    }

    @Test
    fun statusMessage_followsProximity() {
        assertEquals(StatusMessage.NoOfficeSet, ready.copy(proximity = ProximityResult.NoOfficeSet).statusMessage)
        assertEquals(StatusMessage.AcquiringFix, ready.copy(proximity = ProximityResult.AcquiringFix).statusMessage)
        assertEquals(
            StatusMessage.LocationUnavailable,
            ready.copy(proximity = ProximityResult.LocationUnavailable(LocationError.ProviderFailure)).statusMessage,
        )
        assertEquals(StatusMessage.InRange(10), ready.statusMessage)
    }

    @Test
    fun statusMessage_roundsUp_soJustOverTheLimitNeverReadsAsOnIt() {
        assertEquals(StatusMessage.OutOfRange(51), ready.copy(proximity = ProximityResult.OutOfRange(50.1)).statusMessage)
        assertEquals(StatusMessage.InRange(50), ready.copy(proximity = ProximityResult.InRange(50.0)).statusMessage)
    }

    @Test
    fun statusMessage_roundsDistancesToTheNearestMetre_withoutFloatingPointNoise() {
        assertEquals(StatusMessage.OutOfRange(120), ready.copy(proximity = ProximityResult.OutOfRange(120.00000000001)).statusMessage)
        assertEquals(StatusMessage.OutOfRange(121), ready.copy(proximity = ProximityResult.OutOfRange(120.6)).statusMessage)
        assertEquals(StatusMessage.InRange(12), ready.copy(proximity = ProximityResult.InRange(11.6)).statusMessage)
    }

    @Test
    fun statusMessage_forAnUnmeasurableDistance_isLocationUnavailable() {
        assertEquals(
            StatusMessage.LocationUnavailable,
            ready.copy(proximity = ProximityResult.OutOfRange(Double.NaN)).statusMessage,
        )
    }

    @Test
    fun statusMessage_whenInRangeButNotAllowedToMark_explainsWhy() {
        val window = AttendanceWindow(LocalTime.of(9, 0), LocalTime.of(10, 30))
        assertEquals(
            StatusMessage.OutsideWindow(window.start, window.end),
            ready.copy(isWithinWindow = false).statusMessage,
        )
        assertEquals(
            StatusMessage.AlreadyMarkedToday,
            ready.copy(alreadyMarkedToday = true, isWithinWindow = false).statusMessage,
        )
    }

    @Test
    fun statusMessage_forAnUnusableAccuracy_hasNoNumber() {
        assertEquals(
            StatusMessage.WeakSignal(null),
            ready.copy(proximity = ProximityResult.WeakSignal(Double.NaN)).statusMessage,
        )
    }
}
