package com.monir.preciseattendance.ui.attendance

import androidx.annotation.StringRes

/** One-off effects (consumed once, not part of [AttendanceUiState]). */
sealed interface AttendanceUiEvent {
    data class ShowMessage(@StringRes val messageRes: Int) : AttendanceUiEvent
    data object RequestLocationPermission : AttendanceUiEvent
    data object OpenAppSettings : AttendanceUiEvent
    data object OpenLocationSettings : AttendanceUiEvent
}
