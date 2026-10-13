package com.monir.preciseattendance.ui.attendance

/** One-off effects, consumed once and kept out of [AttendanceUiState]. */
sealed interface AttendanceUiEvent {
    data class ShowMessage(val message: StatusMessage) : AttendanceUiEvent

    /** Ask the UI to check location settings and show the system "turn on location" prompt. */
    data object RequestLocationSettingsResolution : AttendanceUiEvent
    data object OpenAppSettings : AttendanceUiEvent
    data object AttendanceMarked : AttendanceUiEvent
}
