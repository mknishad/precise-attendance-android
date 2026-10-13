package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.ProximityResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceDisplayTest {

    private val ready = AttendanceUiState(
        permissionState = LocationPermissionState.Granted,
        isLocationEnabled = true,
        proximity = ProximityResult.InRange(10.0),
        isWithinWindow = true,
    )

    @Test
    fun rangeIndicator_followsProximity() {
        val expected = mapOf(
            ProximityResult.InRange(10.0) to RangeIndicator.InRange,
            ProximityResult.OutOfRange(80.0) to RangeIndicator.OutOfRange,
            ProximityResult.WeakSignal(50.0) to RangeIndicator.WeakSignal,
            ProximityResult.AcquiringFix to RangeIndicator.Acquiring,
            ProximityResult.NoOfficeSet to RangeIndicator.NoOffice,
            ProximityResult.LocationUnavailable(LocationError.Timeout) to RangeIndicator.Unavailable,
        )
        for ((proximity, indicator) in expected) {
            assertEquals("$proximity", indicator, ready.copy(proximity = proximity).rangeIndicator())
        }
    }

    @Test
    fun tones_matchTheDesign() {
        assertEquals(Tone.Error, RangeIndicator.OutOfRange.tone)
        assertEquals(Tone.Success, RangeIndicator.InRange.tone)
        assertEquals(Tone.Warning, RangeIndicator.WeakSignal.tone)
        assertEquals(Tone.Neutral, RangeIndicator.Acquiring.tone)
        assertEquals(Tone.Neutral, RangeIndicator.NoOffice.tone)
    }

    @Test
    fun checkInMode_isReadyOnlyWhenMarkingIsEnabled() {
        assertEquals(CheckInMode.Ready, ready.checkInMode())
        assertEquals(CheckInMode.Locked, ready.copy(isWithinWindow = false).checkInMode())
        assertEquals(CheckInMode.Locked, ready.copy(proximity = ProximityResult.OutOfRange(80.0)).checkInMode())
        assertEquals(CheckInMode.Locked, ready.copy(isLocationEnabled = false).checkInMode())
    }

    @Test
    fun checkInMode_isMarkedOnceAttendanceIsRecorded_whateverElseIsTrue() {
        assertEquals(CheckInMode.Marked, ready.copy(alreadyMarkedToday = true).checkInMode())
        assertEquals(
            CheckInMode.Marked,
            ready.copy(alreadyMarkedToday = true, proximity = ProximityResult.OutOfRange(80.0)).checkInMode(),
        )
    }

    @Test
    fun mainArea_putsPermissionThenLocationBeforeTheRange() {
        assertEquals(MainArea.Range, ready.mainArea())
        assertEquals(MainArea.LocationOff, ready.copy(isLocationEnabled = false).mainArea())
        for (permission in LocationPermissionState.entries - LocationPermissionState.Granted) {
            assertEquals(
                "$permission",
                MainArea.PermissionNeeded,
                ready.copy(permissionState = permission, isLocationEnabled = false).mainArea(),
            )
        }
    }

    @Test
    fun displayDistance_isWholeMetres_andNeverContradictsTheLimit() {
        assertEquals(120, ready.copy(proximity = ProximityResult.OutOfRange(120.00000000001)).displayDistanceMeters)
        assertEquals(51, ready.copy(proximity = ProximityResult.OutOfRange(50.1)).displayDistanceMeters)
        assertEquals(50, ready.copy(proximity = ProximityResult.InRange(50.0)).displayDistanceMeters)
        assertEquals(0, ready.copy(proximity = ProximityResult.InRange(0.2)).displayDistanceMeters)
    }

    @Test
    fun displayDistance_isNullWithoutATrustworthyMeasurement() {
        assertEquals(null, ready.copy(proximity = ProximityResult.WeakSignal(40.0)).displayDistanceMeters)
        assertEquals(null, ready.copy(proximity = ProximityResult.AcquiringFix).displayDistanceMeters)
        assertEquals(null, ready.copy(proximity = ProximityResult.OutOfRange(Double.NaN)).displayDistanceMeters)
    }
}
