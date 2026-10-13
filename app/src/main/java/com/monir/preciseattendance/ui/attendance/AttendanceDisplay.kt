package com.monir.preciseattendance.ui.attendance

import androidx.annotation.StringRes
import com.monir.preciseattendance.R
import com.monir.preciseattendance.domain.model.ProximityResult

/** How worried the screen should look; resolved to colours by the theme. */
enum class Tone { Success, Error, Warning, Neutral }

/** The range status as the ring and chip show it. */
enum class RangeIndicator(val tone: Tone, @StringRes val chipLabel: Int, @StringRes val ringLabel: Int) {
    InRange(Tone.Success, R.string.chip_in_range, R.string.ring_label_away),
    OutOfRange(Tone.Error, R.string.chip_out_of_range, R.string.ring_label_away),
    WeakSignal(Tone.Warning, R.string.chip_weak_signal, R.string.ring_label_weak_signal),
    Acquiring(Tone.Neutral, R.string.chip_acquiring, R.string.ring_label_locating),
    NoOffice(Tone.Neutral, R.string.chip_no_office, R.string.ring_label_no_office),
    Unavailable(Tone.Warning, R.string.chip_location_unavailable, R.string.ring_label_unavailable),
}

fun AttendanceUiState.rangeIndicator(): RangeIndicator = when (proximity) {
    is ProximityResult.InRange -> RangeIndicator.InRange
    is ProximityResult.OutOfRange -> RangeIndicator.OutOfRange
    is ProximityResult.WeakSignal -> RangeIndicator.WeakSignal
    ProximityResult.AcquiringFix -> RangeIndicator.Acquiring
    ProximityResult.NoOfficeSet -> RangeIndicator.NoOffice
    is ProximityResult.LocationUnavailable -> RangeIndicator.Unavailable
}

/** What the check-in card should look like. */
enum class CheckInMode { Locked, Ready, Marked }

fun AttendanceUiState.checkInMode(): CheckInMode = when {
    alreadyMarkedToday -> CheckInMode.Marked
    isMarkAttendanceEnabled -> CheckInMode.Ready
    else -> CheckInMode.Locked
}

/** Which block fills the main area: permission and location services come before everything else. */
enum class MainArea { PermissionNeeded, LocationOff, Range }

fun AttendanceUiState.mainArea(): MainArea = when {
    permissionState != LocationPermissionState.Granted -> MainArea.PermissionNeeded
    !isLocationEnabled -> MainArea.LocationOff
    else -> MainArea.Range
}
